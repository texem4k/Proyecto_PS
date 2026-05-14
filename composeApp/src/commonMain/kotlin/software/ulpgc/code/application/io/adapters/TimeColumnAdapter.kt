package software.ulpgc.code.application.io.adapters

import app.cash.sqldelight.ColumnAdapter
import software.ulpgc.code.architecture.model.times.Time
import software.ulpgc.code.architecture.model.times.TimeFactory

object TimeColumnAdapter: ColumnAdapter<Time, String> {
    override fun decode(databaseValue: String): Time {
        return TimeFactory().parse(databaseValue)
    }

    override fun encode(value: Time): String {
        return value.toString()
    }
}