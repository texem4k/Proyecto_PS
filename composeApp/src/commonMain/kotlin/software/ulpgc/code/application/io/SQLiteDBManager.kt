package software.ulpgc.code.application.io

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

class SQLiteDBManager(databaseDriverFactory: DatabaseDriverFactory, private val seedData: JSONParser.DBData) : DBManager {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.appDatabaseQueries

    init {
        fillTablesIfEmpty()
    }

    fun fillTablesIfEmpty() {
        if (dbQuery.countUsers().executeAsOne() != 0L) {
            return
        }
        seedData.users.forEach { dbQuery.insertUser(it.id.toString(), it.name) }
        insert(seedData.dbObjects())
    }

    override fun topics(): List<Topic> {
        return dbQuery.getTopics{ id, name, color -> Topic(name, color.toInt(), Uuid.parse(id), DBState.DEFAULT) }.executeAsList()
    }

    override fun tags(): List<Tag> {
        return dbQuery.getTags{ id, name, topicId -> Tag(name,Uuid.parse(topicId), Uuid.parse(id), DBState.DEFAULT) }.executeAsList()
    }

    override fun tasks(): List<Task> {
        return dbQuery.getTasks { id, name, priority, userId, description, interval, topicId ->
            val time = dbQuery.getTimeFor(id, { id, _, type, start, end -> TimeFactory().createTime(Instant.parse(start), Instant.parse(end), type.toInt(), Uuid.parse(id)) }).executeAsOne()
            val tagList = dbQuery.getTagsFor(id) { _, tagId -> Uuid.parse(tagId) }.executeAsList().toMutableList()
            Task(priority.toInt(), name, Uuid.parse(userId), description, Uuid.parse(topicId), time, TaskInterval.entries[interval.toInt()], tagList, Uuid.parse(id), DBState.DEFAULT)
        }.executeAsList()
    }

    override fun insert(objects: Sequence<DBObject>) {
        database.transaction {
            objects.forEach(::insertDBObject)
        }
    }

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

    override fun update(objects: Sequence<DBObject>) {
        database.transaction {
            objects.forEach(::updateDBObject)
        }
    }

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

    override fun delete(objects: Sequence<DBObject>) {
        database.transaction {
            objects.forEach(::deleteDBObject)
        }
    }

    private fun deleteDBObject(obj: DBObject) {
        when (obj) {
            is Topic -> dbQuery.deleteTopic(obj.id.toString())
            is Tag -> dbQuery.deleteTag(obj.id.toString())
            is Task -> dbQuery.deleteTask(obj.id.toString())
        }
    }
}