package software.ulpgc.code.architecture.io

import software.ulpgc.code.architecture.model.Group
import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.Topic
import software.ulpgc.code.architecture.model.User
import software.ulpgc.code.architecture.model.tasks.CompletionStat

interface DBManager {
    suspend fun insert(objects: Sequence<DBObject>): Result<Unit>
    suspend fun update(objects: Sequence<DBObject>): Result<Unit>
    suspend fun delete(objects: Sequence<DBObject>): Result<Unit>

    suspend fun topics(): Result<List<Topic>>
    suspend fun tags(): Result<List<Tag>>
    suspend fun tasks(): Result<List<Task>>
    suspend fun groups(): Result<List<Group>>
    suspend fun users(): Result<List<User>>
    suspend fun completionStats(): Result<List<CompletionStat>>
}
