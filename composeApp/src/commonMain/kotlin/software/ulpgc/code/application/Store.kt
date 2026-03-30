package software.ulpgc.code.application

import kotlinx.coroutines.*
import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.Task
import software.ulpgc.code.architecture.model.Topic

class Store constructor(val manager: DBManager): Storage {

    private val tasks: MutableList<Task> = mutableListOf()
    private val topics: MutableList<Topic> = mutableListOf()
    private val tags: MutableList<Tag> = mutableListOf()

    private val storeScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        startDBAutoStore()
    }

    private fun startDBAutoStore(intervalMs: Long = 30_000L) {
        storeScope.launch {
            while (isActive) {
                delay(intervalMs)
                storeRequired()
            }
        }
    }

    private fun storeRequired() {
        insertRequired(
            topics.asSequence().filter { topic.state == State.NEW },
            tags.asSequence().filter { tag.state == State.NEW },
            tasks.asSequence().filter { task.state == State.NEW }
        )
        updateRequired(
            topics.asSequence().filter { topic.state == State.UPDATED },
            tags.asSequence().filter { tag.state == State.UPDATED },
            tasks.asSequence().filter { task.state == State.UPDATED }
        )
        deleteRequired(
            topics.asSequence().filter { topic.state == State.DELETED },
            tags.asSequence().filter { tag.state == State.DELETED },
            tasks.asSequence().filter { task.state == State.DELETED }
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
        topics.forEach { topic.state = State.DEFAULT }
        tags.forEach { tags.state = State.DEFAULT }
        tasks.forEach { tasks.state = State.DEFAULT }
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

    override fun tasks(): Sequence<Task> {
        return this.tasks.asSequence().filter { task.state != State.DELETED }
    }

    override fun topics(): Sequence<Topic> {
        return this.topics.asSequence().filter { topic.state != State.DELETED}
    }

    override fun tags(): Sequence<Tag> {
        return this.tags.asSequence().filter { tag.state != State.DELETED }
    }

    override fun addTask(task: Task) {
        this.tasks.add(task)
    }

    override fun addTopic(topic: Topic) {
        this.topics.add(topic)
    }

    override fun addTags(vararg tags: Tag) {
        this.tags.addAll(tags)
    }

    private fun removeTasks(tasks: List<Task>) {
        this.tasks.removeAll(tasks)
    }

    private fun removeTopics(topics: List<Topic>) {
        this.topics.removeAll(topics)
    }

    private fun removeTags(tags: List<Tag>) {
        this.tags.removeAll(tags)
    }
}
