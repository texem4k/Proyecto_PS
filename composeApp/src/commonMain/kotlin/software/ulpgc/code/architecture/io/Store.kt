package software.ulpgc.code.architecture.io

import io.github.jan.supabase.auth.status.SessionSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import software.ulpgc.code.architecture.control.exceptions.AppException
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
        return groups().first { it.id == Storage.currentGroup}
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

    fun topics(): Sequence<Topic> = Storage.topics.asSequence().filterNot { it.isLocalDeleted() || it.isCloudDeleted() }.filter { it.groupId == Storage.currentGroup }

    fun tags(): Sequence<Tag> = Storage.tags.asSequence().filterNot { it.isLocalDeleted() || it.isCloudDeleted() }.filter { tag -> topics().any{ it.id == tag.topicId} }

    fun tasks(): Sequence<Task> = Storage.tasks.asSequence().filterNot { it.isLocalDeleted() || it.isCloudDeleted() }.filter { tasks -> topics().any{ it.id == tasks.topicId} }

    fun groups(): Sequence<Group> = Storage.groups.asSequence().filterNot { it.isLocalDeleted() || it.isCloudDeleted() }

    fun users(): Sequence<User> = Storage.users.asSequence().filterNot { it.isLocalDeleted() || it.isCloudDeleted() }

    fun completions(): Sequence<CompletionStat> = Storage.stats.asSequence().filterNot { it.isLocalDeleted() || it.isCloudDeleted() }

    fun addTopic(topic: Topic) {
        Storage.topics.add(topic)
    }

    fun addTag(tag: Tag) {
        Storage.tags.add(tag)
    }

    fun addTask(task: Task) {
        Storage.tasks.add(task)
    }

    fun addGroup(group: Group) {
        Storage.groups.add(group)
    }

    fun addUser(user: User) {
        Storage.users.add(user)
    }

    fun addCompletionStat(completionStat: CompletionStat) {
        Storage.stats.add(completionStat)
    }

    suspend fun onLogOut() {
        Storage.restartCurrent()
        users().filterNot { it.id == currentUser() }
            .forEach { it.localDBState = DBState.DELETED }
        groups().filterNot { it.id == currentGroup() }
            .forEach { it.localDBState = DBState.DELETED }
        LocalDBStore.execute()
        Storage.clearAll()
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

    fun clearAll(){
        groups.removeAll { it.isLocalCleared() }
        users.removeAll { it.isLocalCleared() }
        topics.removeAll { it.isLocalCleared() }
        tags.removeAll { it.isLocalCleared() }
        tasks.removeAll { it.isLocalCleared() }
        stats.removeAll { it.isLocalCleared() }
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