package software.ulpgc.code.application.io

import software.ulpgc.code.architecture.io.DBManager
import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.Task
import software.ulpgc.code.architecture.model.Topic
import software.ulpgc.db.AppDatabase

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
        seedData.users.forEach { dbQuery.insertUser(it.id.toLong(), it.name) }
        insert(
            seedData.topics.asSequence().map{ topicData -> Topic() },
            seedData.tags.asSequence().map{ tagData -> Tag() },
            seedData.tasks.asSequence().map{ tasksData -> Task() }
        )
        TODO("ESPERANDO AL NEGRO")
    }

    override fun topics(): List<Topic> {
        return dbQuery.getTopics{ id, name, color -> Topic() }.executeAsList()
    }

    override fun tags(): List<Tag> {
        return dbQuery.getTags{ id, name, topicId -> Tag() }.executeAsList()
    }

    override fun tasks(): List<Task> {
        return dbQuery.getTasks { id, type, name, userId, description, topicId -> Task() }.executeAsList()
    }

    override fun insert(topics: Sequence<Topic>, tags: Sequence<Tag>, tasks: Sequence<Task>) {
        database.transaction {
            topics.forEach { topic -> dbQuery.insertTopic(1L, "A", 1L) }
            tags.forEach { tag -> dbQuery.insertTag(1L, "B", 1L) }
            tasks.forEach { task ->
                dbQuery.insertTask(1L, 1L, "C", 1L, "A", 1L)
                dbQuery.insertTime(1L, 1L, 1L, "A", "A")
                task.tags.forEach { tag -> dbQuery.insertTaskTag(1L, 1L) }
            }
        }
    }

    override fun update(topics: Sequence<Topic>, tags: Sequence<Tag>, tasks: Sequence<Task>) {
        database.transaction {
            topics.forEach { topic -> dbQuery.updateTopic("A", 1L, 1L) }
            tags.forEach { tag -> dbQuery.updateTag("B", 1L, 1L) }
            tasks.forEach { task ->
                dbQuery.updateTask(1L, "C", 1L, "A", 1L, 1L)
                dbQuery.updateTime(1L, "a", "a", 1L)
                dbQuery.deleteTaskTagsForTask(1L)
                task.tags.forEach { tag -> dbQuery.insertTaskTag(1L, 1L) }
            }
        }
    }

    override fun delete(topics: Sequence<Topic>, tags: Sequence<Tag>, tasks: Sequence<Task>) {
        database.transaction {
            topics.forEach { topic -> dbQuery.deleteTopic(1L) }
            tags.forEach { tag -> dbQuery.deleteTag(1L) }
            tasks.forEach { task -> dbQuery.deleteTask(1L) }
        }
    }
}