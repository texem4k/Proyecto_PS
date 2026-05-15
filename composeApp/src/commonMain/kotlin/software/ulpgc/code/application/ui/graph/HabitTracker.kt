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
import androidx.compose.ui.text.font.FontWeight
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
import com.patrykandpatrick.vico.multiplatform.common.component.rememberTextComponent
import software.ulpgc.code.architecture.model.tasks.Task

enum class ChartMode { DAILY, WEEKLY, MONTHLY, QUARTERLY,ANNUAL }

@Composable
fun HabitTrackerChart(
    tasks: List<Task>,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(ChartMode.DAILY) }

    val stats = remember(tasks, mode) {
        when (mode) {
            ChartMode.DAILY -> aggregateByDay(tasks)
            ChartMode.WEEKLY -> aggregateByWeek(tasks)
            ChartMode.MONTHLY -> aggregateByMonth(tasks)
            ChartMode.QUARTERLY -> aggregateByQuarter(tasks)
            ChartMode.ANNUAL -> aggregateByYear(tasks)
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

        TaskCompletionChart(stats = stats, isWeekly = mode == ChartMode.WEEKLY)
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
    stats: List<DayStats>,
    modifier: Modifier = Modifier
) {
    val proposedColor = Color(0xFF5C6BC0)
    val completedColor = Color(0xFF26A69A)
    val textColor = Color.White
    val gridColor = Color.White.copy(alpha = 0.15f)
    val axisColor = Color.White.copy(alpha = 0.5f)

    if (stats.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f),
            contentAlignment = Alignment.Center
        ) { Text("Sin datos en este período") }
        return
    }

    val model = remember(stats) {
        val xValues = stats.indices.map { it.toFloat() }
        CartesianChartModel(
            ColumnCartesianLayerModel.build {
                series(x = xValues, y = stats.map { it.proposed.toFloat() })
                series(x = xValues, y = stats.map { it.completed.toFloat() })
            }
        )
    }

    val bottomAxisFormatter: CartesianValueFormatter = remember(stats) {
        CartesianValueFormatter { _, value, _ ->
            val idx = value.toInt()
            if (idx in stats.indices) {
                val d = stats[idx].date
                "${d.dayOfMonth}/${d.monthNumber}"
            } else ""
        }
    }

    Surface(
        color = Color(0xFF0D0D0D),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(all = 16.dp)
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(fill = Fill(proposedColor)),
                        rememberLineComponent(fill = Fill(completedColor))
                    )
                ),
                startAxis = VerticalAxis.rememberStart(
                    itemPlacer = VerticalAxis.ItemPlacer.step(step = { 1.0 }),
                    label = rememberAxisLabelComponent(
                        style = TextStyle(color = textColor, fontSize = 11.sp)
                    ),
                    line = rememberAxisLineComponent(fill = Fill(axisColor)),
                    tick = rememberAxisTickComponent(fill = Fill(axisColor)),
                    guideline = rememberAxisGuidelineComponent(fill = Fill(gridColor)),
                    titleComponent = rememberTextComponent(
                        style = TextStyle(
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    ),
                    title = "Nº de tareas",
                    valueFormatter = { _, value, _ -> value.toInt().toString() }
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    itemPlacer = HorizontalAxis.ItemPlacer.aligned(spacing = { 1 }),
                    label = rememberAxisLabelComponent(
                        style = TextStyle(color = textColor, fontSize = 10.sp)
                    ),
                    line = rememberAxisLineComponent(fill = Fill(axisColor)),
                    tick = rememberAxisTickComponent(fill = Fill(axisColor)),
                    guideline = rememberAxisGuidelineComponent(fill = Fill(gridColor)),
                    valueFormatter = bottomAxisFormatter
                )
            ),
            model = model,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 16.dp)
                .background(Color.Transparent)
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