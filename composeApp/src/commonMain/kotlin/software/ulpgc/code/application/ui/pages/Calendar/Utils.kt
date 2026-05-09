package software.ulpgc.code.application.ui.pages.Calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import software.ulpgc.code.application.ui.filters.TaskFilters
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.Priority
import kotlin.sequences.forEach
import kotlin.time.Clock

public val HOUR_HEIGHT = 64.dp

public val TIME_COL_W = 52.dp
public val START_HOUR = 0
public val END_HOUR = 24

@Composable
fun WeekDayColumn(
    date: LocalDate,
    entries: List<SampleEntry>,
    isToday: Boolean,
    isSelected: Boolean,
    hourHeight: Dp,
    onEntryClick: (SampleEntry) -> Unit,
    urgencyColor: Color? = null
) {
    val totalHours = END_HOUR - START_HOUR

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.White)
    ) {
        for (h in 0 until totalHours) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.65.dp)
                    .offset(y = (h * hourHeight.value).dp)
                    .background(Color.Black.copy(alpha = 0.2f))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .offset(y = (h * hourHeight.value + hourHeight.value / 2).dp)
                    .background(Color.Black.copy(alpha = 0.1f))
            )
        }

        entries.forEach { entry ->
            val task = entry.task
            if (task != null) {
                val startDate = task.time.start.toLocalDateTime(TimeZone.currentSystemDefault()).date
                val endDate = task.time.end.toLocalDateTime(TimeZone.currentSystemDefault()).date

                var (startH, endH) = parseEntryTime(entry.time)

                when (date) {
                    startDate -> {
                        endH = END_HOUR.toFloat()
                    }

                    endDate -> {
                        startH = START_HOUR.toFloat()
                    }

                    else -> {
                        startH = START_HOUR.toFloat()
                        endH = END_HOUR.toFloat()
                    }
                }
                WeekEventChip(
                    entry = entry,
                    startHour = startH,
                    endHour = endH,
                    hourHeight = hourHeight,
                    onClick = { onEntryClick(entry) }
                )
            }
        }

        if (isToday) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val nowFraction = now.hour + now.minute / 60f
            if (nowFraction in START_HOUR.toFloat()..END_HOUR.toFloat()) {
                val topDp = ((nowFraction - START_HOUR) * hourHeight.value).dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = topDp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .offset(x = (-4).dp, y = (-4).dp)
                            .background(Color(0xFF4F6EF7), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(Color(0xFF4F6EF7))
                    )
                }
            }
        }

        if (urgencyColor != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(urgencyColor)
            )
        }
    }
}



@Composable
fun WeekEventChip(
    entry: SampleEntry,
    startHour: Float,
    endHour: Float,
    hourHeight: Dp,
    onClick: () -> Unit = {}
) {
    val topDp = ((startHour - START_HOUR) * hourHeight.value).dp
    val heightDp = ((endHour - startHour) * hourHeight.value).dp.coerceAtLeast(20.dp)
    val timeData = entry.task!!.time!!.mostrar().split(",")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
            .offset(y = topDp)
            .height(heightDp)
            .clip(RoundedCornerShape(5.dp))
            .background(entry.color.copy(alpha = 0.13f))
            .border(
                width = 2.5.dp,
                color = entry.color,
                shape = RoundedCornerShape(topStart = 5.dp, bottomStart = 5.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 5.dp, vertical = 3.dp)
    ) {
        Column {
            Text(
                text = entry.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = entry.color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Desde: ${timeData[0]} - ${timeData[1]}\n" +
                        "Hasta: ${timeData[2]} - ${timeData[3]}",
                fontSize = 12.sp,
                lineHeight = 14.sp,
                color = entry.color.copy(alpha = 0.8f),
                maxLines = 8
            )
        }
    }
}

fun parseEntryTime(time: String): Pair<Float, Float> {
    return try {
        val parts = time.split("·").map { it.trim() }
        val start = parts[0].split(":").let { it[0].toFloat() + it[1].toFloat() / 60f }
        val end = if (parts.size > 1) parts[1].split(":").let { it[0].toFloat() + it[1].toFloat() / 60f }
        else start + 0.5f
        start to end
    } catch (_: Exception) {
        9f to 9.5f
    }
}

fun getFilteredEntries(store: Storage, filters: TaskFilters): Map<LocalDate, List<SampleEntry>> {
    val topicsById = store.topics().associateBy { it.id }
    val tasks = store.tasks()
    val map = mutableMapOf<LocalDate, MutableList<SampleEntry>>()

    tasks.forEach { task ->
        val startDate = task.time.start.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val endDate = task.time.end.toLocalDateTime(TimeZone.currentSystemDefault()).date

        var current = startDate
        while (current <= endDate) {
            val startTime = task.time.start.toLocalDateTime(TimeZone.currentSystemDefault())
            val endTime = task.time.end.toLocalDateTime(TimeZone.currentSystemDefault())
            val topicColor = (topicsById[task.topicId]?.color ?: 0xFF9E9E9E.toInt()) or 0xFF000000.toInt()

            val entry = SampleEntry(
                title = task.name,
                time = "${startTime.hour.toString().padStart(2, '0')}:${
                    startTime.minute.toString().padStart(2, '0')
                } · " +
                        "${endTime.hour.toString().padStart(2, '0')}:${endTime.minute.toString().padStart(2, '0')}",
                color = Color(topicColor),
                task = task
            )

            map.getOrPut(current) { mutableListOf() }.add(entry)
            current = current.plus(1, DateTimeUnit.DAY)
        }
    }

    if (!filters.hasFilter) return map.mapValues { (_, entries) -> entries.toList() }.toMap()
    return map.mapValues { (_, entries) ->
        entries.filter { entry ->
            val task = entry.task ?: return@filter false
            val topicName = store.topics().find { it.id == task.topicId }?.name.orEmpty()
            val tagNames = task.tags.mapNotNull { id -> store.tags().find { it.id == id }?.name }.toSet()
            val statusOk = filters.status.isEmpty() || filters.status.any { selected ->
                when (selected) {
                        "Completadas" -> task.isCompleted
                    "No completadas" -> !task.isCompleted
                    else -> true
                }
            }

            val priorityOk = filters.priority.isEmpty() || filters.priority.any { selectedText ->
                Priority.entries.firstOrNull { it.text == selectedText }?.values?.contains(task.priority) == true
            }
            val topicOk = filters.topics.isEmpty() || filters.topics.contains(topicName)
            val tagsOk = filters.tags.isEmpty() || filters.tags.any { selected -> tagNames.contains(selected) }

            statusOk && priorityOk && topicOk && tagsOk
        }
    }.filterValues { it.isNotEmpty() }

}
