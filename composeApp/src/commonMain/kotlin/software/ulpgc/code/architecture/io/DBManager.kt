package software.ulpgc.code.architecture.io

import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.Task
import software.ulpgc.code.architecture.model.Topic

interface DBManager {
    fun insert(topics: Sequence<Topic>, tags: Sequence<Tag>, tasks: Sequence<Task>)
    fun update(topics: Sequence<Topic>, tags: Sequence<Tag>, tasks: Sequence<Task>)
    fun delete(topics: Sequence<Topic>, tags: Sequence<Tag>, tasks: Sequence<Task>)

    fun topics(): List<Topic>
    fun tags(): List<Tag>
    fun tasks(): List<Task>
}
