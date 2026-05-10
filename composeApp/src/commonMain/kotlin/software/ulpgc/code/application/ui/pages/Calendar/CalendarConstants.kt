package software.ulpgc.code.application.ui.pages.Calendar

import androidx.compose.ui.graphics.Color

object CalendarConstants {
    val MONTH_NAMES_ES = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    val DAY_LETTERS = listOf("L", "M", "X", "J", "V", "S", "D")

    val DAY_NAMES_ES = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

    val LEGEND_ITEMS_CALENDAR = listOf(
        "Sin tareas" to Color.Transparent,
        "Baja prioridad" to Color(red = 0f, green = 1f, blue = 0f, alpha = 0.8f),
        "Prioridad media" to Color(red = 0.5f, green = 0.5f, blue = 0f, alpha = 0.8f),
        "Alta prioridad" to Color(red = 1f, green = 0f, blue = 0f, alpha = 0.8f)
    )
}