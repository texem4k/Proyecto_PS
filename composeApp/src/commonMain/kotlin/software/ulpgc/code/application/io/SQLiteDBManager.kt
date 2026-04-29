package software.ulpgc.code.application.io

import software.ulpgc.code.architecture.control.exceptions.DBException
import software.ulpgc.code.architecture.io.DBManager
import software.ulpgc.code.architecture.io.DBObject
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.times.TimeFactory
import software.ulpgc.code.architecture.model.Topic
import software.ulpgc.code.architecture.model.tasks.TaskInterval
import software.ulpgc.db.AppDatabase
import kotlin.time.Instant
import kotlin.uuid.Uuid

class SQLiteDBManager(databaseDriverFactory: DatabaseDriverFactory, private val seedData: DBData) : DBManager {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.appDatabaseQueries

    init {
        fillTablesIfEmpty()
    }

    fun fillTablesIfEmpty() {
        if (dbQuery.countUsers().executeAsOne() != 0L) return
        seedData.users.forEach { dbQuery.insertUser(it.id.toString(), it.name) }
        insert(seedData.dbObjects()).getOrThrow()
    }

    override fun topics(): Result<List<Topic>> = runCatching {
        dbQuery.getTopics { id, name, color ->
            Topic(name, color.toInt(), Uuid.parse(id), DBState.DEFAULT)
        }.executeAsList()
    }.mapDBException("Failed to fetch topics")

    override fun tags(): Result<List<Tag>> = runCatching {
        dbQuery.getTags { id, name, topicId ->
            Tag(name, Uuid.parse(topicId), Uuid.parse(id), DBState.DEFAULT)
        }.executeAsList()
    }.mapDBException("Failed to fetch tags")

    override fun tasks(): Result<List<Task>> = runCatching {
        val raws = dbQuery.getTasks().executeAsList()
        raws.map { raw ->
            val time = dbQuery.getTimeFor(
                raw.id
            ) { id, _, type, start, end ->
                TimeFactory().createTime(
                    Instant.parse(start),
                    Instant.parse(end),
                    type.toInt(),
                    Uuid.parse(id)
                )
            }.executeAsOne()

            val tagList = dbQuery.getTagsFor(raw.id) { _, tagId ->
                Uuid.parse(tagId)
            }.executeAsList().toMutableList()

            Task(
                raw.priority.toInt(),
                raw.name,
                Uuid.parse(raw.userId),
                raw.description,
                Uuid.parse(raw.topicId),
                time,
                TaskInterval.entries[raw.interval.toInt()],
                tagList.toMutableSet(),
                Uuid.parse(raw.id),
                DBState.DEFAULT
            )
        }
    }.mapDBException("Failed to fetch tasks")

    override fun insert(objects: Sequence<DBObject>): Result<Unit> = runCatching {
        database.transaction {
            objects.forEach(::insertDBObject)
        }
    }.mapDBException("Failed to insert objects")

    private fun insertDBObject(obj: DBObject) {
        when (obj) {
            is Topic -> dbQuery.insertTopic(obj.id.toString(), obj.name, obj.color.toLong())
            is Tag -> dbQuery.insertTag(obj.id.toString(), obj.name, obj.topicId.toString())
            is Task -> {
                dbQuery.insertTask(
                    obj.id.toString(), obj.name, obj.priority.toLong(), obj.userId.toString(),
                    obj.description, obj.interval.ordinal.toLong(), obj.topicId.toString()
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
        }
    }

    override fun update(objects: Sequence<DBObject>): Result<Unit> = runCatching {
        database.transaction {
            objects.forEach(::updateDBObject)
        }
    }.mapDBException("Failed to update objects")

    private fun updateDBObject(obj: DBObject) {
        when (obj) {
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
        }
    }

    override fun delete(objects: Sequence<DBObject>): Result<Unit> = runCatching {
        database.transaction {
            objects.forEach(::deleteDBObject)
        }
    }.mapDBException("Failed to delete objects")

    private fun deleteDBObject(obj: DBObject) {
        when (obj) {
            is Topic -> dbQuery.deleteTopic(obj.id.toString())
            is Tag -> dbQuery.deleteTag(obj.id.toString())
            is Task -> dbQuery.deleteTask(obj.id.toString())
        }
    }

    private fun <T> Result<T>.mapDBException(msg: String): Result<T> =
        mapFailure { cause -> DBException("$msg: ${cause.message}") }

    private fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> =
        onFailure { return Result.failure(transform(it)) }.let { this }
}