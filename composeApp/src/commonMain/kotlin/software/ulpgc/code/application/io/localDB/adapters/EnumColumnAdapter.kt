package software.ulpgc.code.application.io.localDB.adapters

import app.cash.sqldelight.ColumnAdapter

class EnumColumnAdapter<T : Enum<T>>(
    private val enumValues: Array<out T>,
) : ColumnAdapter<T, Long> {
    override fun decode(databaseValue: Long): T {
        return enumValues[databaseValue.toInt()]
    }

    override fun encode(value: T): Long {
      return value.ordinal.toLong()
    }
}

inline fun <reified T : Enum<T>> EnumColumnAdapter(): EnumColumnAdapter<T> {
    return EnumColumnAdapter(enumValues())
}