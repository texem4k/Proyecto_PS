package software.ulpgc.code.application.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.time.Clock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp


private val ColorSuccess = Color(0xFF1D9E75)
private val ColorInfo    = Color(0xFF378ADD)
private val ColorWarning = Color(0xFFBA7517)
private val ColorDanger  = Color(0xFFA32D2D)

data class UserKpiState(
    val completedToday: Int,
    val completedThisWeek: Int,
    val pending: Int,
    val overdue: Int,
    val plannedToday: Int,
    val plannedThisWeek: Int,
    val completionRateToday: Float?,
    val completionRateWeek: Float?,
) {
    companion object {
        val Empty = UserKpiState(
            completedToday = 0,
            completedThisWeek = 0,
            pending = 0,
            overdue = 0,
            plannedToday = 0,
            plannedThisWeek = 0,
            completionRateToday = null,
            completionRateWeek = null,
        )
    }
}


@Composable
fun rememberUserKpi(
    store: Storage,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): State<UserKpiState> {

    return remember(timeZone) {
        derivedStateOf {
            val tasks = store.tasks()
            computeUserKpi(
                tasks = tasks.toList(),
                now = Clock.System.now(),
                timeZone = timeZone,
            )
        }
    }
}

fun computeUserKpi(
    tasks: List<Task>,
    now: Instant = kotlin.time.Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): UserKpiState {

    if (tasks.isEmpty()) return UserKpiState.Empty

    val today = now.toLocalDateTime(timeZone).date
    val weekStart = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
    val weekEnd   = weekStart.plus(6, DateTimeUnit.DAY)

    fun Instant.toLocalDate() = toLocalDateTime(timeZone).date

    fun Task.endDate() = time.end.toLocalDate()

    fun Task.isToday()     = endDate() == today
    fun Task.isThisWeek()  = endDate() in weekStart..weekEnd
    fun Task.isOverdue()   = !isCompleted && time.end < now

    val completedToday    = tasks.count { it.isCompleted && it.isToday() }
    val completedThisWeek = tasks.count { it.isCompleted && it.isThisWeek() }

    val pending = tasks.count { !it.isCompleted && !it.isOverdue() }
    val overdue = tasks.count { it.isOverdue() }

    val plannedToday     = tasks.count { it.isToday() }
    val plannedThisWeek  = tasks.count { it.isThisWeek() }

    val completionRateToday = if (plannedToday > 0)
        (completedToday.toFloat() / plannedToday) * 100f
    else null

    val completionRateWeek = if (plannedThisWeek > 0)
        (completedThisWeek.toFloat() / plannedThisWeek) * 100f
    else null

    return UserKpiState(
        completedToday        = completedToday,
        completedThisWeek     = completedThisWeek,
        pending               = pending,
        overdue               = overdue,
        plannedToday          = plannedToday,
        plannedThisWeek       = plannedThisWeek,
        completionRateToday   = completionRateToday,
        completionRateWeek    = completionRateWeek,
    )
}


@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun KpiCard(
    label: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    progress: Float? = null,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .background(accentColor),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (progress != null) {
                Spacer(Modifier.height(2.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = accentColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}




@Composable
fun KpiDashboard(
    kpi: UserKpiState,
    modifier: Modifier = Modifier,
) {
    val rateToday = kpi.completionRateToday
    val rateWeek  = kpi.completionRateWeek

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SectionLabel("Hoy")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KpiCard(
                label       = "Completadas hoy",
                value       = "${kpi.completedToday}",
                subtitle    = "de ${kpi.plannedToday} planificadas",
                accentColor = ColorSuccess,
                progress    = if (kpi.plannedToday > 0) kpi.completedToday / kpi.plannedToday.toFloat() else null,
                modifier    = Modifier.weight(1f).heightIn(min = 110.dp),
            )
            KpiCard(
                label       = "Cumplimiento hoy",
                value       = rateToday?.let { "${it.toInt()}%" } ?: "—",
                subtitle    = if (rateToday != null) "${kpi.completedToday} de ${kpi.plannedToday}" else "sin datos",
                accentColor = ColorInfo,
                progress    = rateToday?.let { it / 100f },
                modifier    = Modifier.weight(1f).heightIn(min = 110.dp),
            )
        }

        SectionLabel("Esta semana")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KpiCard(
                label       = "Completadas semana",
                value       = "${kpi.completedThisWeek}",
                subtitle    = "de ${kpi.plannedThisWeek} planificadas",
                accentColor = ColorSuccess,
                progress    = if (kpi.plannedThisWeek > 0) kpi.completedThisWeek / kpi.plannedThisWeek.toFloat() else null,
                modifier    = Modifier.weight(1f).heightIn(min = 110.dp),
            )
            KpiCard(
                label       = "Cumplimiento semana",
                value       = rateWeek?.let { "${it.toInt()}%" } ?: "—",
                subtitle    = if (rateWeek != null) "${kpi.completedThisWeek} de ${kpi.plannedThisWeek}" else "sin datos",
                accentColor = ColorInfo,
                progress    = rateWeek?.let { it / 100f },
                modifier    = Modifier.weight(1f).heightIn(min = 110.dp),
            )
        }

        SectionLabel("Estado general")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KpiCard(
                label       = "Pendientes",
                value       = "${kpi.pending}",
                subtitle    = "sin vencer",
                accentColor = ColorWarning,
                modifier    = Modifier.weight(1f).heightIn(min = 90.dp),
            )
            KpiCard(
                label       = "Vencidas",
                value       = "${kpi.overdue}",
                subtitle    = "requieren atención",
                accentColor = ColorDanger,
                modifier    = Modifier.weight(1f).heightIn(min = 90.dp),
            )
        }
    }
}