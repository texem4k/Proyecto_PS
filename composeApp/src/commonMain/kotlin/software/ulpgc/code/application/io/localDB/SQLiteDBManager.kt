package software.ulpgc.code.application.io.localDB

import androidx.compose.ui.graphics.Color
import software.ulpgc.code.application.io.localDB.adapters.ColorColumnAdapter
import software.ulpgc.code.application.io.localDB.adapters.EnumColumnAdapter
import software.ulpgc.code.application.io.localDB.adapters.InstantColumnAdapter
import software.ulpgc.code.application.io.localDB.adapters.StringColumnAdapter
import software.ulpgc.code.application.io.localDB.adapters.TimeColumnAdapter
import software.ulpgc.code.application.io.localDB.adapters.UuidColumnAdapter
import software.ulpgc.code.architecture.control.exceptions.DBException
import software.ulpgc.code.architecture.io.DBManager
import software.ulpgc.code.architecture.io.DBObject
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.model.Group
import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.Topic
import software.ulpgc.code.architecture.model.User
import software.ulpgc.code.architecture.model.tasks.CompletionStat
import software.ulpgc.db.AppDatabase
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
            dateAdapter = InstantColumnAdapter,
            idAdapter = UuidColumnAdapter
        ),
        TaskTagAdapter = DBTaskTag.Adapter(
            taskIdAdapter = UuidColumnAdapter,
            tagIdAdapter = UuidColumnAdapter,
            topicIdAdapter = UuidColumnAdapter
        ),
        TopicAdapter = DBTopic.Adapter(
            idAdapter = UuidColumnAdapter,
            groupIdAdapter = UuidColumnAdapter,
            nameAdapter = StringColumnAdapter,
            colorAdapter = ColorColumnAdapter
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
        dbQuery.getTopics { id: Uuid, groupId: Uuid, name: String, color: Color ->
            Topic(name, color, groupId, id, DBState.DEFAULT)
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
        dbQuery.getTaskCompletions { id, taskId, date, completed ->
            CompletionStat(taskId, completed, date, id, DBState.DEFAULT)
        }.executeAsList()
    }.mapDBException("Failed to fetch completion stats")

    override fun insert(objects: Sequence<DBObject>): Result<Unit> = runCatching {
        database.transaction {
            objects.forEach(::insertDBObject)
        }
    }.mapDBException("Failed to insert objects")

    private fun insertDBObject(obj: DBObject) {
        when (obj) {
            is User -> dbQuery.insertUser(obj.id, obj.name)
            is Group -> {
                dbQuery.insertGroup(obj.id, obj.name, obj.description)
                obj.users.forEach { user -> dbQuery.insertUserIntoGroup(user.key, obj.id, user.value) }
            }
            is Topic -> dbQuery.insertTopic(obj.id, obj.groupId, obj.name, obj.color)
            is Tag -> dbQuery.insertTag(obj.id,obj.topicId, obj.name)
            is Task -> {
                dbQuery.insertTask(
                    obj.id, obj.topicId,
                    obj.name, obj.description,
                    obj.time, obj.interval,
                    obj.priority, obj.isCompleted,
                )
                obj.tags.forEach { tag -> dbQuery.insertTagForTask(obj.id, tag) }
                obj.users.forEach { user -> dbQuery.insertUserForTask(obj.id, user) }
            }
            is CompletionStat -> dbQuery.insertTaskCompletion(obj.id, obj.taskId, obj.date, obj.completed)
        }
    }

    override fun update(objects: Sequence<DBObject>): Result<Unit> = runCatching {
        database.transaction {
            objects.forEach(::updateDBObject)
        }
    }.mapDBException("Failed to update objects")

    private fun updateDBObject(obj: DBObject) {
        when (obj) {
            is User -> dbQuery.updateUser(obj.name, obj.id)
            is Group -> {
                dbQuery.updateGroup(obj.name, obj.description, obj.id)
                dbQuery.deleteUsersFromGroup(obj.id)
                obj.users.forEach { user -> dbQuery.insertUserIntoGroup(user.key, obj.id, user.value)}
            }
            is Topic -> dbQuery.updateTopic(obj.name, obj.color, obj.id)
            is Tag -> dbQuery.updateTag(obj.name, obj.id)
            is Task -> {
                dbQuery.updateTask(
                    obj.name,
                    obj.description,
                    obj.time,
                    obj.interval,
                    obj.priority,
                    obj.isCompleted,
                    obj.id
                )
                dbQuery.deleteTagsForTask(obj.id)
                dbQuery.deleteUsersForTask(obj.id)
                obj.tags.forEach { tag -> dbQuery.insertTagForTask(obj.id, tag) }
                obj.users.forEach { user -> dbQuery.insertUserForTask(obj.id, user) }
            }
            is CompletionStat -> dbQuery.updateTaskCompletion(obj.date, obj.completed, obj.id)
        }
    }

    override fun delete(objects: Sequence<DBObject>): Result<Unit> = runCatching {
        database.transaction {
            objects.forEach(::deleteDBObject)
        }
    }.mapDBException("Failed to delete objects")

    private fun deleteDBObject(obj: DBObject) {
        when (obj) {
            is User -> dbQuery.deleteUser(obj.id)
            is Group -> dbQuery.deleteGroup(obj.id)
            is Topic -> dbQuery.deleteTopic(obj.id)
            is Tag -> dbQuery.deleteTag(obj.id)
            is Task -> dbQuery.deleteTask(obj.id)
            is CompletionStat -> dbQuery.deleteTaskCompletion(obj.id)
        }
    }

    private fun <T> Result<T>.mapDBException(msg: String): Result<T> =
        mapFailure { cause -> DBException("$msg: ${cause.message}") }

    private fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> =
        onFailure { return Result.failure(transform(it)) }.let { this }
}