package software.ulpgc.code.architecture.io

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import software.ulpgc.code.architecture.control.coroutines.Coroutinable
import software.ulpgc.code.architecture.control.coroutines.CoroutineManager
import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.model.*
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.tasks.TaskMonitor

class Store (private val manager: DBManager, private val onFailLoad: Unit): Storage,
    Coroutinable {

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

    override fun topics(): Sequence<Topic> = this.topics.asSequence().filter { ! it.isDeleted() }

    override fun tags(): Sequence<Tag> = this.tags.asSequence().filter { ! it.isDeleted() }

    override fun tasks(): Sequence<Task> = this.tasks.asSequence().filter { ! it.isDeleted() }

    override fun addTopics(topics: List<Topic>) {
        this.topics.addAll(topics)
    }

    override fun addTags(tags: List<Tag>) {
        this.tags.addAll(tags)
    }

    override fun addTasks(tasks: List<Task>) {
        this.tasks.addAll(tasks)
    }

    override val delayMilis: Long = 60_000L

    override suspend fun onInit() {
        LogMaster.log("Cargando datos BD")
        loadDBData()
        LogMaster.log("Finalizado carga de datos BD")
        _ready.value = true
        TaskMonitor(this)
    }

    private fun loadDBData() {
        try {
            addTopics(manager.topics().getOrThrow())
            addTags(manager.tags().getOrThrow())
            addTasks(manager.tasks().getOrThrow())
        } catch (e: Exception) {
            onFailLoad(e.message)
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