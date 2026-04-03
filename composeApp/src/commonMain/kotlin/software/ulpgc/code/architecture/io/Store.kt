package software.ulpgc.code.architecture.io

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import software.ulpgc.code.architecture.model.*

class Store constructor(val manager: DBManager): Storage {

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
        insertRequired(
            topics.asSequence().filter { DBState.NEW.isNew(it) },
            tags.asSequence().filter { DBState.NEW.isNew(it) },
            tasks.asSequence().filter { DBState.NEW.isNew(it) }
        )
        updateRequired(
            topics.asSequence().filter { DBState.UPDATED.isUpdated(it) },
            tags.asSequence().filter { DBState.UPDATED.isUpdated(it) },
            tasks.asSequence().filter { DBState.UPDATED.isUpdated(it) }
        )
        deleteRequired(
            topics.asSequence().filter { DBState.DELETED.isDeleted(it) },
            tags.asSequence().filter { DBState.DELETED.isDeleted(it) },
            tasks.asSequence().filter { DBState.DELETED.isDeleted(it) }
        )
    }

    private fun insertRequired(topics: Sequence<Topic>, tags: Sequence<Tag>, tasks: Sequence<Task>) {
        manager.insert(topics, tags, tasks)
        setDefaultState(topics,tags,tasks)
    }

    private fun updateRequired(topics: Sequence<Topic>, tags: Sequence<Tag>, tasks: Sequence<Task>) {
        manager.update(topics, tags, tasks)
        setDefaultState(topics,tags,tasks)
    }

    private fun setDefaultState(topics: Sequence<Topic>, tags: Sequence<Tag>, tasks: Sequence<Task>) {
        topics.forEach { it.dbState = DBState.DEFAULT }
        tags.forEach { it.dbState = DBState.DEFAULT }
        tasks.forEach { it.dbState = DBState.DEFAULT }
    }

    private fun deleteRequired(topics: Sequence<Topic>, tags: Sequence<Tag>, tasks: Sequence<Task>) {
        manager.delete(topics, tags, tasks)
        removeTopics(topics.toList())
        removeTags(tags.toList())
        removeTasks(tasks.toList())
    }

    fun dispose() {
        storeScope.cancel()
        storeRequired()
    }

    override fun topics(): Sequence<Topic> {
        return this.topics.asSequence().filter { ! DBState.DELETED.isDeleted(it)}
    }

    override fun tags(): Sequence<Tag> {
        return this.tags.asSequence().filter { ! DBState.DELETED.isDeleted(it) }
    }

    override fun tasks(): Sequence<Task> {
        return this.tasks.asSequence().filter { ! DBState.DELETED.isDeleted(it) }
    }

    override fun addTopics(topics: List<Topic>) {
        this.topics.addAll(topics)
    }

    override fun addTags(tags: List<Tag>) {
        this.tags.addAll(tags)
    }

    override fun addTasks(tasks: List<Task>) {
        this.tasks.addAll(tasks)
    }

    private fun removeTopics(topics: List<Topic>) {
        this.topics.removeAll(topics)
    }

    private fun removeTags(tags: List<Tag>) {
        this.tags.removeAll(tags)
    }

    private fun removeTasks(tasks: List<Task>) {
        this.tasks.removeAll(tasks)
    }
}