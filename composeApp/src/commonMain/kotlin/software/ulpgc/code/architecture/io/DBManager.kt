package software.ulpgc.code.architecture.io

import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.Topic

interface DBManager {
    fun insert(objects: Sequence<DBObject>)
    fun update(objects: Sequence<DBObject>)
    fun delete(objects: Sequence<DBObject>)

    fun topics(): List<Topic>
    fun tags(): List<Tag>
    fun tasks(): List<Task>
}
