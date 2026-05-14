package software.ulpgc.code.architecture.io

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import software.ulpgc.code.architecture.control.coroutines.Coroutinable
import software.ulpgc.code.architecture.control.coroutines.CoroutineManager
import software.ulpgc.code.architecture.control.exceptions.AppException
import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.model.*
import software.ulpgc.code.architecture.model.tasks.CompletionStat
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.uuid.Uuid

class Store (private val manager: DBManager, private val onFailLoad: (AppException) -> Unit, private val afterLoad: (Store) -> Unit): Storage,
    Coroutinable {

    private var currentGroup: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000000")
    private var currentUser: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000000")

    private val topics: MutableSet<Topic> = mutableSetOf()
    private val tags: MutableSet<Tag> = mutableSetOf()
    private val tasks: MutableSet<Task> = mutableSetOf()
    private val groups: MutableSet<Group> = mutableSetOf()
    private val users: MutableSet<User> = mutableSetOf()
    private val stats: MutableSet<CompletionStat> = mutableSetOf()

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    init {
        CoroutineManager.add(this)
    }

    private fun insertRequired(objects: Sequence<DBObject>) {
        manager.insert(objects)
        objects.forEach { it.dbState = DBState.DEFAULT }
    }

    private fun updateRequired(objects: Sequence<DBObject>) {
        manager.update(objects)
        objects.forEach { it.dbState = DBState.DEFAULT }
    }

    private fun deleteRequired(objects: Sequence<DBObject>) {
        manager.delete(objects)
        cleanLists()
    }

    private fun cleanLists() {
        topics.removeAll { it.isDeleted() }
        tags.removeAll { it.isDeleted() }
        tasks.removeAll { it.isDeleted() }
    }

    override fun currentGroup(): Group {
        return this.groups().first { it.id == this.currentGroup}
    }

    override fun changeGroupTo(group: Group) {
        this.currentGroup = group.id
    }

    override fun currentUser(): User {
        return this.users.first { it.id == this.currentUser }
    }

    override fun topics(): Sequence<Topic> = this.topics.asSequence().filterNot { it.isDeleted() }.filter { it.groupId == currentGroup }

    override fun tags(): Sequence<Tag> = this.tags.asSequence().filterNot { it.isDeleted() }.filter { tag -> topics().any{ it.id == tag.topicId} }

    override fun tasks(): Sequence<Task> = this.tasks.asSequence().filterNot { it.isDeleted() }.filter { tasks -> topics().any{ it.id == tasks.topicId} }

    override fun groups(): Sequence<Group> = this.groups.asSequence().filterNot { it.isDeleted() }

    override fun users(): Sequence<User> = this.users.asSequence().filterNot { it.isDeleted() }

    override fun completions(): Sequence<CompletionStat> = this.stats.asSequence().filterNot { it.isDeleted() }

    override fun addTopic(topic: Topic) {
        this.topics.add(topic)
    }

    override fun addTag(tag: Tag) {
        this.tags.add(tag)
    }

    override fun addTask(task: Task) {
        this.tasks.add(task)
    }

    override fun addGroup(group: Group) {
        this.groups.add(group)
    }

    override fun addUser(user: User) {
        this.users.add(user)
    }

    override fun addCompletionStat(completionStat: CompletionStat) {
        this.stats.add(completionStat)
    }

    override val delayMilis: Long = 60_000L

    override suspend fun onInit() {
        LogMaster.log("Cargando datos BD")
        loadDBData()
        LogMaster.log("Finalizado carga de datos BD")
        _ready.value = true
        afterLoad(this)
    }

    private fun loadDBData() {
        try {
            manager.users().getOrThrow().forEach { addUser(it) }
            manager.groups().getOrThrow().forEach { addGroup(it) }
            manager.topics().getOrThrow().forEach{ addTopic(it) }
            manager.tags().getOrThrow().forEach { addTag(it) }
            manager.tasks().getOrThrow().forEach { addTask(it) }
            manager.completionStats().getOrThrow().forEach { addCompletionStat(it)}
        } catch (e: AppException) {
            onFailLoad(e)
        }
    }

    override suspend fun execute() {
        deleteRequired(dbObjects().filter { it.isDeleted() })
        updateRequired(dbObjects().filter { it.isUpdated() })
        insertRequired(dbObjects().filter { it.isNew() })
    }

    private fun dbObjects(): Sequence<DBObject> = users.asSequence() + groups.asSequence() + topics.asSequence() + tags.asSequence() + tasks.asSequence() + stats.asSequence()

    override suspend fun onDispose() {
        execute()
        LogMaster.log("Parando guardado automático")
    }
}