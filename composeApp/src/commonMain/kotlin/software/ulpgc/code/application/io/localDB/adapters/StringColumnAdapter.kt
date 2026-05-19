package software.ulpgc.code.application.io.localDB.adapters

import app.cash.sqldelight.ColumnAdapter

object StringColumnAdapter: ColumnAdapter<String, String> {
    override fun decode(databaseValue: String): String {
        return databaseValue
    }

    override fun encode(value: String): String {
        return value
    }
}