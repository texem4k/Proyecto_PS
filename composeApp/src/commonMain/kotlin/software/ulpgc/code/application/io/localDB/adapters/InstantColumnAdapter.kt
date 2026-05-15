package software.ulpgc.code.application.io.localDB.adapters

import app.cash.sqldelight.ColumnAdapter
import kotlin.time.Instant

object InstantColumnAdapter: ColumnAdapter<Instant, String> {
    override fun decode(databaseValue: String): Instant {
        return Instant.parse(databaseValue)
    }

    override fun encode(value: Instant): String {
        return value.toString()
    }
}