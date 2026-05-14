package software.ulpgc.code.architecture.io

import software.ulpgc.code.architecture.model.Group
import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.Topic
import software.ulpgc.code.architecture.model.User
import software.ulpgc.code.architecture.model.tasks.CompletionStat

interface DBManager {
    fun insert(objects: Sequence<DBObject>): Result<Unit>
    fun update(objects: Sequence<DBObject>): Result<Unit>
    fun delete(objects: Sequence<DBObject>): Result<Unit>

    fun topics(): Result<List<Topic>>
    fun tags(): Result<List<Tag>>
    fun tasks(): Result<List<Task>>
    fun groups(): Result<List<Group>>
    fun users(): Result<List<User>>
    fun completionStats(): Result<List<CompletionStat>>
}
