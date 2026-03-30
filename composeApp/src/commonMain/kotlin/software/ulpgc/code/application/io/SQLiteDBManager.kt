package software.ulpgc.code.application.io

import software.ulpgc.code.architecture.io.DBManager
import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.Task
import software.ulpgc.code.architecture.model.Topic
import software.ulpgc.db.AppDatabase

class SQLiteDBManager(databaseDriverFactory: DatabaseDriverFactory) : DBManager {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.appDatabaseQueries

    init {

    }

    override fun topics(): List<Topic> {
        TODO("Not yet implemented")
    }

    override fun tags(): List<Tag> {
        TODO("Not yet implemented")
    }

    override fun tasks(): List<Task> {
        TODO("Not yet implemented")
    }

    override fun insert(topics: Sequence<Topic>, tags: Sequence<Tag>, tasks: Sequence<Task>) {
        TODO("Not yet implemented")
    }

    override fun update(topics: Sequence<Topic>, tags: Sequence<Tag>, tasks: Sequence<Task>) {
        TODO("Not yet implemented")
    }

    override fun delete(topics: Sequence<Topic>, tags: Sequence<Tag>, tasks: Sequence<Task>) {
        TODO("Not yet implemented")
    }
}