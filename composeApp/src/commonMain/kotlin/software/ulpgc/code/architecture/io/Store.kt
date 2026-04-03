package software.ulpgc.code.architecture.io

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import software.ulpgc.code.architecture.model.*
import software.ulpgc.code.architecture.model.tasks.Task

class Store (val manager: DBManager): Storage {

    private val topics: MutableList<Topic> = mutableListOf()
    private val tags: MutableList<Tag> = mutableListOf()
    private val tasks: MutableList<Task> = mutableListOf()

    private val storeScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        loadDBData()
        startDBAutoStore()
    }

    private fun loadDBData() {
        addTopics(manager.topics())
        addTags(manager.tags())
        addTasks(manager.tasks())
    }

    private fun startDBAutoStore(intervalMs: Long = 60_000L) {
        storeScope.launch {
            while (isActive) {
                delay(intervalMs)
                storeRequired()
            }
        }
    }

    private fun storeRequired() {
        insertRequired(dbObjects().filter { it.isNew() })
        updateRequired(dbObjects().filter { it.isUpdated() })
        deleteRequired(dbObjects().filter { it.isDeleted() })
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
        storeScope.cancel()
        storeRequired()
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