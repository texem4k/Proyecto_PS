package software.ulpgc.code.application.io.localDB.adapters

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import app.cash.sqldelight.ColumnAdapter

object ColorColumnAdapter: ColumnAdapter<Color, Long> {
    override fun decode(databaseValue: Long): Color {
        return Color(databaseValue)
    }

    override fun encode(value: Color): Long {
        return value.toArgb().toLong()
    }
}