package software.ulpgc.code.architecture.io

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import software.ulpgc.code.architecture.control.exceptions.AppException
import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.model.*
import software.ulpgc.code.architecture.model.tasks.CompletionStat
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.collections.asSequence
import kotlin.uuid.Uuid

object Store {
    val ready: StateFlow<Boolean> = Storage.ready.asStateFlow()
    fun initialize(localManager: DBManager, onFailLoad: (AppException) -> Unit, afterLoad: () -> Unit, cloudManager: DBManager, canUseCloudDB: () -> Boolean) {
        LocalDBStore.initialize(
            localManager,
            Storage::cleanLists,
            {
                Storage.ready.value = true
                afterLoad()
            },
            onFailLoad,
            Storage::dbObjects
        )
        CloudDBStore.initialize(
            cloudManager,
            Storage::cleanLists,
            Storage::dbObjects,
            canUseCloudDB
        )
    }
    
    fun currentGroup(): Group {
        return groups().first { it.id == Storage.currentGroup }
    }

    fun changeGroupTo(group: Group) {
        Storage.currentGroup = group.id
    }

    fun currentUser(): Uuid {
        return Storage.currentUser
    }

    fun changeUserTo(userId: Uuid) {
        Storage.currentUser = userId
    }

    fun topics(): Sequence<Topic> = Storage.topics.asSequence().filterNot { it.isLocalDeleted() }.filter { it.groupId == Storage.currentGroup }

    fun tags(): Sequence<Tag> = Storage.tags.asSequence().filterNot { it.isLocalDeleted() }.filter { tag -> topics().any{ it.id == tag.topicId} }

    fun tasks(): Sequence<Task> = Storage.tasks.asSequence().filterNot { it.isLocalDeleted() }.filter { tasks -> topics().any{ it.id == tasks.topicId} }

    fun groups(): Sequence<Group> = Storage.groups.asSequence().filterNot { it.isLocalDeleted() }

    fun users(): Sequence<User> = Storage.users.asSequence().filterNot { it.isLocalDeleted() }

    fun completions(): Sequence<CompletionStat> = Storage.stats.asSequence().filterNot { it.isLocalDeleted() }

    fun <T: DBObject> add(obj: T) {
        when (obj) {
            is Group -> Storage.groups.add(obj)
            is User -> Storage.users.add(obj)
            is Topic -> Storage.topics.add(obj)
            is Tag -> Storage.tags.add(obj)
            is Task -> Storage.tasks.add(obj)
            is CompletionStat -> Storage.stats.add(obj)
        }
    }

    fun <T: DBObject> tryFind(obj: T): T? {
        return when (obj) {
            is Group -> Storage.groups.find { obj.id == it.id }
            is User -> Storage.users.find { obj.id == it.id }
            is Topic -> Storage.topics.find { obj.id == it.id }
            is Tag -> Storage.tags.find { obj.id == it.id }
            is Task -> Storage.tasks.find { obj.id == it.id }
            is CompletionStat -> Storage.stats.find { obj.id == it.id }
            else -> null
        } as T?
    }

    suspend fun onLogOut() {
        Storage.restartCurrent()
        Storage.clearAll()
        LocalDBStore.onInit()
    }
}

private object Storage {
    var currentGroup: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000000")
    var currentUser: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000000")

    val topics: MutableSet<Topic> = mutableSetOf()
    val tags: MutableSet<Tag> = mutableSetOf()
    val tasks: MutableSet<Task> = mutableSetOf()
    val groups: MutableSet<Group> = mutableSetOf()
    val users: MutableSet<User> = mutableSetOf()
    val stats: MutableSet<CompletionStat> = mutableSetOf()

    val ready = MutableStateFlow(false)

    fun dbObjects(): Sequence<DBObject> = users.asSequence() + groups.asSequence() + topics.asSequence() + tags.asSequence() + tasks.asSequence() + stats.asSequence()

    fun clearCloud(){
        groups.removeAll { it.id != currentGroup }
        users.removeAll { it.id != currentUser }
    }

    fun clearAll(){
        groups.clear()
        users.clear()
        topics.clear()
        tags.clear()
        tasks.clear()
        stats.clear()
    }

    fun cleanLists() {
        groups.removeAll { it.isLocalCleared() && it.isCloudCleared() }
        users.removeAll { it.isLocalCleared() && it.isCloudCleared() }
        topics.removeAll { it.isLocalCleared() && it.isCloudCleared() }
        tags.removeAll { it.isLocalCleared() && it.isCloudCleared() }
        tasks.removeAll { it.isLocalCleared() && it.isCloudCleared() }
        stats.removeAll { it.isLocalCleared() && it.isCloudCleared() }
    }

    fun restartCurrent(){
        this.currentGroup = Uuid.parse("00000000-0000-0000-0000-000000000000")
        this.currentUser = Uuid.parse("00000000-0000-0000-0000-000000000000")
    }
}