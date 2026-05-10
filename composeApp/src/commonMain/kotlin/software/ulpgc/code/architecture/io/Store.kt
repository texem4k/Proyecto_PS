package software.ulpgc.code.architecture.io

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import software.ulpgc.code.architecture.control.coroutines.Coroutinable
import software.ulpgc.code.architecture.control.coroutines.CoroutineManager
import software.ulpgc.code.architecture.control.exceptions.AppException
import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.model.*
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.uuid.Uuid

class Store (private val manager: DBManager, private val onFailLoad: (AppException) -> Unit, private val afterLoad: (Store) -> Unit): Storage,
    Coroutinable {

    private var currentGroup: Uuid = Uuid.parse("ROOTID") //Se le debe asignar la variable global del grupo que se esta viendo
    private val topics: MutableSet<Topic> = mutableSetOf()
    private val tags: MutableSet<Tag> = mutableSetOf()
    private val tasks: MutableSet<Task> = mutableSetOf()

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

    override fun topics(): Sequence<Topic> = this.topics.asSequence().filterNot { it.isDeleted() }.filter { it.groupId == currentGroup }

    override fun tags(): Sequence<Tag> = this.tags.asSequence().filterNot { it.isDeleted() }.filter { tag -> topics().any{ it.id == tag.topicId} }

    override fun tasks(): Sequence<Task> = this.tasks.asSequence().filterNot { it.isDeleted() }.filter { tasks -> topics().any{ it.id == tasks.topicId} }

    override fun addTopic(topic: Topic) {
        this.topics.add(topic)
    }

    override fun addTag(tag: Tag) {
        this.tags.add(tag)
    }

    override fun addTask(task: Task) {
        this.tasks.add(task)
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
            manager.topics().getOrThrow().forEach{ addTopic(it) }
            manager.tags().getOrThrow().forEach { addTag(it) }
            manager.tasks().getOrThrow().forEach { addTask(it) }
        } catch (e: AppException) {
            onFailLoad(e)
        }
    }

    override suspend fun execute() {
        deleteRequired(dbObjects().filter { it.isDeleted() })
        updateRequired(dbObjects().filter { it.isUpdated() })
        insertRequired(dbObjects().filter { it.isNew() })
    }

    private fun dbObjects(): Sequence<DBObject> = topics.asSequence() + tags.asSequence() + tasks.asSequence()

    override suspend fun onDispose() {
        execute()
        LogMaster.log("Parando guardado automático")
    }
}