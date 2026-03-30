package software.ulpgc.code.architecture.io

import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.Task
import software.ulpgc.code.architecture.model.Topic

interface Storage {
    fun topics(): Sequence<Topic>
    fun tags(): Sequence<Tag>
    fun tasks(): Sequence<Task>

    fun addTopics(topics: List<Topic>)
    fun addTags(tags: List<Tag>)
    fun addTasks(tasks: List<Task>)
}