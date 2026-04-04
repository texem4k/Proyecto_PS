package software.ulpgc.code.architecture.io

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import software.ulpgc.code.architecture.model.*
import software.ulpgc.code.architecture.model.tasks.Task

class Store (private val managerFactory: () -> DBManager, private val dbDispatcher: CoroutineDispatcher = Dispatchers.Default): Storage {

    private val topics: MutableSet<Topic> = mutableSetOf()
    private val tags: MutableSet<Tag> = mutableSetOf()
    private val tasks: MutableSet<Task> = mutableSetOf()

    private val storeScope = CoroutineScope(dbDispatcher + SupervisorJob())

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    private lateinit var manager: DBManager

    init {
        startDBAutoStore()
    }

    private fun startDBAutoStore(intervalMs: Long = 60_000L) {
        storeScope.launch {
            manager = managerFactory()
            loadDBData()
            _ready.value = true
            while (isActive) {
                delay(intervalMs)
                storeRequired()
            }
        }
    }

    private suspend fun loadDBData() = withContext(dbDispatcher) {
        addTopics(manager.topics())
        addTags(manager.tags())
        addTasks(manager.tasks())
    }

    private suspend fun storeRequired() = withContext(dbDispatcher) {
        deleteRequired(dbObjects().filter { it.isDeleted() })
        updateRequired(dbObjects().filter { it.isUpdated() })
        insertRequired(dbObjects().filter { it.isNew() })
    }

    private fun dbObjects(): Sequence<DBObject> = topics.asSequence() + tags.asSequence() + tasks.asSequence()

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

    fun dispose() {
        storeScope.launch {
            storeRequired()
        }.invokeOnCompletion {
            storeScope.cancel()
        }
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
}