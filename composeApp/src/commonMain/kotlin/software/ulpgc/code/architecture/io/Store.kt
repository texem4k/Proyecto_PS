package software.ulpgc.code.architecture.io

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
    fun initialize(manager: DBManager, onFailLoad: (AppException) -> Unit, afterLoad: () -> Unit) {
        LocalDBStore.initialize(
            manager,
            Storage::cleanLists,
            {
                Storage.ready.value = true
                afterLoad()
            },
            onFailLoad,
            Storage::dbObjects
        )
    }
    
    fun currentGroup(): Group {
        return groups().first { it.id == Storage.currentGroup}
    }

    fun changeGroupTo(group: Group) {
        Storage.currentGroup = group.id
    }

    fun currentUser(): User {
        return users().first { it.id == Storage.currentUser }
    }

    fun topics(): Sequence<Topic> = Storage.topics.asSequence().filterNot { it.isDeleted() }.filter { it.groupId == Storage.currentGroup }

    fun tags(): Sequence<Tag> = Storage.tags.asSequence().filterNot { it.isDeleted() }.filter { tag -> topics().any{ it.id == tag.topicId} }

    fun tasks(): Sequence<Task> = Storage.tasks.asSequence().filterNot { it.isDeleted() }.filter { tasks -> topics().any{ it.id == tasks.topicId} }

    fun groups(): Sequence<Group> = Storage.groups.asSequence().filterNot { it.isDeleted() }

    fun users(): Sequence<User> = Storage.users.asSequence().filterNot { it.isDeleted() }

    fun completions(): Sequence<CompletionStat> = Storage.stats.asSequence().filterNot { it.isDeleted() }

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

    fun cleanLists() {
        topics.removeAll { it.isDeleted() }
        tags.removeAll { it.isDeleted() }
        tasks.removeAll { it.isDeleted() }
        stats.removeAll { it.isDeleted() }
    }
}