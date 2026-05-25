package software.ulpgc.code

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import software.ulpgc.code.application.io.localDB.DatabaseDriverFactory
import software.ulpgc.db.AppDatabase
import java.util.Properties

class JavaDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        val properties = Properties().apply {
            put("foreign_keys", "true")
        }
        return JdbcSqliteDriver("jdbc:sqlite:app.db", properties, AppDatabase.Schema)
    }
}