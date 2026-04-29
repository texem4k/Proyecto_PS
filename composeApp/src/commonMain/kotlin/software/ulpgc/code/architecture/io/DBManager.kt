package software.ulpgc.code.architecture.io

import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.Topic

interface DBManager {
    fun insert(objects: Sequence<DBObject>): Result<Unit>
    fun update(objects: Sequence<DBObject>): Result<Unit>
    fun delete(objects: Sequence<DBObject>): Result<Unit>

    fun topics(): Result<List<Topic>>
    fun tags(): Result<List<Tag>>
    fun tasks(): Result<List<Task>>
}
