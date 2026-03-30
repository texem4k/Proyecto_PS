package software.ulpgc.code.application

import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.Task
import software.ulpgc.code.architecture.model.Topic

interface Storage {
    fun tasks(): Sequence<Task>
    fun topics(): Sequence<Topic>
    fun tags(): Sequence<Tag>

    fun addTags(vararg tags: Tag)
    fun addTopic(topic: Topic)
    fun addTask(task: Task)
}