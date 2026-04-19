package software.ulpgc.code

import software.ulpgc.code.architecture.io.DBManager
import software.ulpgc.code.architecture.io.DBObject
import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.Topic
import software.ulpgc.code.architecture.model.tasks.Task

class MockDBManager: DBManager {
    override fun insert(objects: Sequence<DBObject>) = Unit
    override fun update(objects: Sequence<DBObject>) = Unit
    override fun delete(objects: Sequence<DBObject>) = Unit

    override fun topics(): List<Topic> = emptyList()
    override fun tags(): List<Tag> = emptyList()
    override fun tasks(): List<Task> = emptyList()
}