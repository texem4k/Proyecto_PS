package software.ulpgc.code

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import software.ulpgc.code.application.io.DatabaseDriverFactory
import software.ulpgc.db.AppDatabase
import java.util.Properties

class JavaDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        return JdbcSqliteDriver("jdbc:sqlite:app.db", Properties(), AppDatabase.Schema)
    }
}