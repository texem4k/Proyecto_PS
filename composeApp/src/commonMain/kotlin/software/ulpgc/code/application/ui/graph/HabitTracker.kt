package software.ulpgc.code.application.ui.graph

import TaskCompletionChart
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.*
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.component.rememberLineComponent
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import software.ulpgc.code.architecture.model.tasks.CompletionStat
import software.ulpgc.code.architecture.model.tasks.Task

enum class ChartMode { DAILY, WEEKLY, MONTHLY, QUARTERLY,ANNUAL }

@Composable
fun HabitTrackerChart(
    taskStat: Sequence<CompletionStat>,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(ChartMode.DAILY) }

    val stats = remember(mode) {
        when (mode) {
            ChartMode.DAILY -> aggregateByDay(taskStat)
            ChartMode.WEEKLY -> aggregateByWeek(taskStat)
            ChartMode.MONTHLY -> aggregateByMonth(taskStat)
            ChartMode.QUARTERLY -> aggregateByQuarter(taskStat)
            ChartMode.ANNUAL -> aggregateByYear(taskStat)
        }
    }

    Column(modifier = modifier) {
        ModeToggle(
            currentMode = mode,
            onModeChange = { mode = it }
        )

        Spacer(Modifier.height(16.dp))

        ChartLegend()

        Spacer(Modifier.height(8.dp))

        TaskCompletionChart(stats = stats, mode= mode)
    }
}

@Composable
fun ModeToggle(currentMode: ChartMode, onModeChange: (ChartMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        ChartMode.entries.forEach { mode ->
            FilterChip(
                selected = currentMode == mode,
                onClick = { onModeChange(mode) },
                label = {
                    Text(
                        when(mode) {
                            ChartMode.DAILY -> "Diario"
                            ChartMode.WEEKLY -> "Semanal"
                            ChartMode.MONTHLY -> "Mensual"
                            ChartMode.QUARTERLY -> "Trimestral"
                            ChartMode.ANNUAL -> "Anual"
                        }
                    )
                },
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun ChartLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = ProposedColor, label = "Propuestas")
        Spacer(Modifier.width(24.dp))
        LegendItem(color = CompletedColor, label = "Completadas")
    }
}

@Composable
fun BarGraph(
    completionStat: Sequence<CompletionStat>,
    tasks: Sequence<Task>,
    modifier: Modifier = Modifier
) {
    val beforeColor = Color(0xFF26A69A)
    val onTimeColor = Color(0xFF5C6BC0)
    val lateColor   = Color(0xFFEF5350)
    val textColor   = Color.White
    val gridColor   = Color.White.copy(alpha = 0.15f)
    val axisColor   = Color.White.copy(alpha = 0.5f)
    val tz          = TimeZone.currentSystemDefault()

    val taskMap = remember(tasks) { tasks.associateBy { it.id } }

    val beforeTotal = remember(completionStat, taskMap) {
        completionStat.count { stat ->
            if (!stat.completed) return@count false
            val deadline = taskMap[stat.taskId]?.time?.end?.toLocalDateTime(tz)?.date ?: return@count false
            stat.endDate.toLocalDateTime(tz).date < deadline
        }
    }

    val onTimeTotal = remember(completionStat, taskMap) {
        completionStat.count { stat ->
            if (!stat.completed) return@count false
            val deadline = taskMap[stat.taskId]?.time?.end?.toLocalDateTime(tz)?.date ?: return@count false
            stat.endDate.toLocalDateTime(tz).date == deadline
        }
    }

    val lateTotal = remember(completionStat, taskMap) {
        completionStat.count { stat ->
            if (!stat.completed) return@count false
            val deadline = taskMap[stat.taskId]?.time?.end?.toLocalDateTime(tz)?.date ?: return@count false
            stat.endDate.toLocalDateTime(tz).date > deadline
        }
    }

    val model = remember(beforeTotal, onTimeTotal, lateTotal) {
        CartesianChartModel(
            ColumnCartesianLayerModel.build {
                series(
                    x = listOf(0f, 1f, 2f),
                    y = listOf(beforeTotal.toFloat(), onTimeTotal.toFloat(), lateTotal.toFloat())
                )
            }
        )
    }

    Surface(
        color = Color(0xFF0D0D0D),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth().padding(16.dp)
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(
                            fill = Fill(beforeColor),
                            thickness = 40.dp
                        ),
                        rememberLineComponent(
                            fill = Fill(onTimeColor),
                            thickness = 40.dp
                        ),
                        rememberLineComponent(
                            fill = Fill(lateColor),
                            thickness = 40.dp
                        )
                    ),
                    columnCollectionSpacing = 8.dp
                ),
                startAxis = VerticalAxis.rememberStart(
                    itemPlacer = VerticalAxis.ItemPlacer.step(step = { 1.0 }),
                    label = rememberAxisLabelComponent(
                        style = TextStyle(color = textColor, fontSize = 11.sp)
                    ),
                    line = rememberAxisLineComponent(fill = Fill(axisColor)),
                    tick = rememberAxisTickComponent(fill = Fill(axisColor)),
                    guideline = rememberAxisGuidelineComponent(fill = Fill(gridColor)),
                    valueFormatter = { _, value, _ -> value.toInt().toString() }
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    itemPlacer = HorizontalAxis.ItemPlacer.aligned(spacing = { 1 }),
                    label = rememberAxisLabelComponent(
                        style = TextStyle(color = textColor, fontSize = 10.sp)
                    ),
                    line = rememberAxisLineComponent(fill = Fill(axisColor)),
                    tick = rememberAxisTickComponent(fill = Fill(axisColor)),
                    guideline = null,
                    valueFormatter = CartesianValueFormatter { _, value, _ ->
                        when (value.toInt()) {
                            0    -> "Antes"
                            1    -> "En plazo"
                            2    -> "Retraso"
                            else -> ""
                        }
                    }
                )
            ),
            model = model,
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

val ProposedColor  = Color(0xFF5C6BC0)
val CompletedColor = Color(0xFF26A69A)