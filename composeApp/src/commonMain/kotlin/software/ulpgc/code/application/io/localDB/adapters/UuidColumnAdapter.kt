package software.ulpgc.code.application.io.localDB.adapters

import app.cash.sqldelight.ColumnAdapter
import kotlin.uuid.Uuid

object UuidColumnAdapter: ColumnAdapter<Uuid, String> {
    override fun decode(databaseValue: String): Uuid {
        return Uuid.parse(databaseValue)
    }

    override fun encode(value: Uuid): String {
        return value.toString()
    }
}