package software.ulpgc.code.application.io

import software.ulpgc.code.application.io.adapters.EnumColumnAdapter
import software.ulpgc.code.application.io.adapters.InstantColumnAdapter
import software.ulpgc.code.application.io.adapters.StringColumnAdapter
import software.ulpgc.code.application.io.adapters.TimeColumnAdapter
import software.ulpgc.code.application.io.adapters.UuidColumnAdapter
import software.ulpgc.code.architecture.control.exceptions.DBException
import software.ulpgc.code.architecture.io.DBManager
import software.ulpgc.code.architecture.io.DBObject
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.model.Group
import software.ulpgc.code.architecture.model.Priority
import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.Topic
import software.ulpgc.code.architecture.model.User
import software.ulpgc.code.architecture.model.tasks.CompletionStat
import software.ulpgc.db.AppDatabase
import software.ulpgc.db.TaskCompletion
import software.ulpgc.db.AssignUserTask as DBAssignUserTask
import software.ulpgc.db.Tag as DBTag
import software.ulpgc.db.Task as DBTask
import software.ulpgc.db.TaskCompletion as DBTaskCompletion
import software.ulpgc.db.TaskTag as DBTaskTag
import software.ulpgc.db.Topic as DBTopic
import software.ulpgc.db.User as DBUser
import software.ulpgc.db.WorkGroup as DBWorkGroup
import software.ulpgc.db.WorkGroupUser as DBWorkGroupUser
import kotlin.uuid.Uuid

class SQLiteDBManager(databaseDriverFactory: DatabaseDriverFactory, private val seedData: DBData) : DBManager {
    private val database = AppDatabase(
        databaseDriverFactory.createDriver(),
        AssignUserTaskAdapter = DBAssignUserTask.Adapter(
            taskIdAdapter = UuidColumnAdapter,
            userIdAdapter = UuidColumnAdapter,
            groupIdAdapter = UuidColumnAdapter
        ),
        TagAdapter = DBTag.Adapter(
            idAdapter = UuidColumnAdapter,
            topicIdAdapter = UuidColumnAdapter,
            nameAdapter = StringColumnAdapter
        ),
        TaskAdapter = DBTask.Adapter(
            idAdapter = UuidColumnAdapter,
            topicIdAdapter = UuidColumnAdapter,
            groupIdAdapter = UuidColumnAdapter,
            nameAdapter = StringColumnAdapter,
            descriptionAdapter = StringColumnAdapter,
            timeAdapter = TimeColumnAdapter,
            intervalAdapter = EnumColumnAdapter(),
            priorityAdapter = EnumColumnAdapter()
        ),
        TaskCompletionAdapter = DBTaskCompletion.Adapter(
            taskIdAdapter = UuidColumnAdapter,
            dateAdapter = InstantColumnAdapter
        ),
        TaskTagAdapter = DBTaskTag.Adapter(
            taskIdAdapter = UuidColumnAdapter,
            tagIdAdapter = UuidColumnAdapter,
            topicIdAdapter = UuidColumnAdapter
        ),
        TopicAdapter = DBTopic.Adapter(
            idAdapter = UuidColumnAdapter,
            groupIdAdapter = UuidColumnAdapter,
            nameAdapter = StringColumnAdapter
        ),
        UserAdapter = DBUser.Adapter(
            idAdapter = UuidColumnAdapter,
            nameAdapter = StringColumnAdapter
        ),
        WorkGroupAdapter = DBWorkGroup.Adapter(
            idAdapter = UuidColumnAdapter,
            nameAdapter = StringColumnAdapter,
            descriptionAdapter = StringColumnAdapter
        ),
        WorkGroupUserAdapter = DBWorkGroupUser.Adapter(
            userIdAdapter = UuidColumnAdapter,
            groupIdAdapter = UuidColumnAdapter,
            privilegeAdapter = EnumColumnAdapter()
        )
    )
    private val dbQuery = database.appDatabaseQueries

    init {
        fillTablesIfEmpty()
    }

    fun fillTablesIfEmpty() {
        if (dbQuery.getUsers().executeAsList().count() != 0) return
        insert(seedData.dbObjects()).getOrThrow()
    }

    override fun topics(): Result<List<Topic>> = runCatching {
        dbQuery.getTopics { id: Uuid, groupId: Uuid, name: String, color: Long ->
            Topic(name, color.toInt(), groupId, id, DBState.DEFAULT)
        }.executeAsList()
    }.mapDBException("Failed to fetch topics")

    override fun tags(): Result<List<Tag>> = runCatching {
        dbQuery.getTags { id, topicId, name ->
            Tag(name, topicId, id, DBState.DEFAULT)
        }.executeAsList()
    }.mapDBException("Failed to fetch tags")

    override fun tasks(): Result<List<Task>> = runCatching {
        val raws = dbQuery.getTasks().executeAsList()
        raws.map { raw ->
            val tags = dbQuery.getTagsFor(raw.id) { _, tagId, _ ->
                tagId
            }.executeAsList().toMutableSet()

            val users = dbQuery.getUsersFor(raw.id) { _, userId, _ ->
                userId
            }.executeAsList().toMutableSet()

            Task(
                raw.topicId,
                raw.name,
                raw.description,
                raw.time,
                raw.interval,
                raw.priority,
                tags,
                users,
                raw.isCompleted,
                raw.id,
                DBState.DEFAULT
            )
        }
    }.mapDBException("Failed to fetch tasks")

    override fun groups(): Result<List<Group>> = runCatching {
        val raws = dbQuery.getGroups().executeAsList()
        raws.map { raw ->
            val users = dbQuery.getUsersInGroups().executeAsList()
                .asSequence().filter { it.groupId == raw.id }.associate{ it.userId to it.privilege }.toMutableMap()

            Group(
                raw.name,
                raw.description,
                users,
                raw.id,
                DBState.DEFAULT
            )
        }
    }.mapDBException("Failed to fetch groups")

    override fun users(): Result<List<User>> = runCatching {
        dbQuery.getUsers { id, name ->
            User(name, id, DBState.DEFAULT)
        }.executeAsList()
    }.mapDBException("Failed to fetch users")

    override fun completionStats(): Result<List<CompletionStat>> = runCatching {
        dbQuery.getTaskCompletions { taskId, date, completed ->
            CompletionStat(taskId, completed, date, DBState.DEFAULT)
        }.executeAsList()
    }.mapDBException("Failed to fetch completion stats")

    override fun insert(objects: Sequence<DBObject>): Result<Unit> = runCatching {
        database.transaction {
            objects.forEach(::insertDBObject)
        }
    }.mapDBException("Failed to insert objects")

    private fun insertDBObject(obj: DBObject) {
        when (obj) {
            is User -> TODO()
            is Group -> TODO()
            is Topic -> dbQuery.insertTopic(obj.id.toString(), obj.name, obj.color.toLong())
            is Tag -> dbQuery.insertTag(obj.id.toString(), obj.name, obj.topicId.toString())
            is Task -> {
                dbQuery.insertTask(
                    obj.id.toString(), obj.name, obj.priority.value , obj.users.forEach { it.toString() },
                    obj.description, obj.interval.ordinal.toLong(), obj.topicId.toString(),
                    obj.isCompleted
                )
                dbQuery.insertTime(
                    obj.time.id.toString(),
                    obj.id.toString(),
                    obj.time.type.toLong(),
                    obj.time.start.toString(),
                    obj.time.end.toString()
                )
                obj.tags.forEach { tag -> dbQuery.insertTaskTag(obj.id.toString(), tag.toString()) }
            }
            is CompletionStat -> TODO()
        }
    }

    override fun update(objects: Sequence<DBObject>): Result<Unit> = runCatching {
        database.transaction {
            objects.forEach(::updateDBObject)
        }
    }.mapDBException("Failed to update objects")

    private fun updateDBObject(obj: DBObject) {
        when (obj) {
            is User -> TODO()
            is Group -> TODO()
            is Topic -> dbQuery.updateTopic(obj.name, obj.color.toLong(), obj.id.toString())
            is Tag -> dbQuery.updateTag(obj.name, obj.topicId.toString(), obj.id.toString())
            is Task -> {
                dbQuery.updateTask(
                    obj.name,
                    obj.userId.toString(),
                    obj.priority.toLong(),
                    obj.description,
                    obj.interval.ordinal.toLong(),
                    obj.topicId.toString(),
                    obj.isCompleted,
                    obj.id.toString()
                )
                dbQuery.updateTime(
                    obj.time.type.toLong(),
                    obj.time.start.toString(),
                    obj.time.end.toString(),
                    obj.time.id.toString()
                )
                dbQuery.deleteTaskTagsForTask(obj.id.toString())
                obj.tags.forEach { tag -> dbQuery.insertTaskTag(obj.id.toString(), tag.toString()) }
            }
            is CompletionStat -> TODO()
        }
    }

    override fun delete(objects: Sequence<DBObject>): Result<Unit> = runCatching {
        database.transaction {
            objects.forEach(::deleteDBObject)
        }
    }.mapDBException("Failed to delete objects")

    private fun deleteDBObject(obj: DBObject) {
        when (obj) {
            is User -> TODO()
            is Group -> TODO()
            is Topic -> dbQuery.deleteTopic(obj.id.toString())
            is Tag -> dbQuery.deleteTag(obj.id.toString())
            is Task -> dbQuery.deleteTask(obj.id.toString())
            is CompletionStat -> TODO()
        }
    }

    private fun <T> Result<T>.mapDBException(msg: String): Result<T> =
        mapFailure { cause -> DBException("$msg: ${cause.message}") }

    private fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> =
        onFailure { return Result.failure(transform(it)) }.let { this }
}