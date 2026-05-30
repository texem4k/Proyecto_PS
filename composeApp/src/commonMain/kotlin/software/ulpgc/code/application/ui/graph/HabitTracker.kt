package software.ulpgc.code.application.ui.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
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
import com.patrykandpatrick.vico.multiplatform.common.component.LineComponent
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import software.ulpgc.code.architecture.model.tasks.CompletionStat
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

enum class ChartMode { DAILY, WEEKLY, MONTHLY, QUARTERLY,ANNUAL }

@Composable
fun HabitTrackerChart(
    taskStat: Sequence<CompletionStat>,
    tasks: Sequence<Task>,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(ChartMode.DAILY) }
    val taskMap = remember(tasks) { tasks.associateBy { it.id } }

    val stats = remember(mode, taskMap) {
        when (mode) {
            ChartMode.DAILY -> aggregateByDay(taskStat)
            ChartMode.WEEKLY -> aggregateByWeek(taskStat)
            ChartMode.MONTHLY -> aggregateByMonth(taskStat)
            ChartMode.QUARTERLY -> aggregateByQuarter(taskStat)
            ChartMode.ANNUAL -> aggregateByYear(taskStat)
        }
    }

    Column(modifier = modifier) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progreso de hábitos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            ModeToggle(
                currentMode = mode,
                onModeChange = { mode = it },
            )
            Spacer(Modifier.weight(1f))

        }

        ChartLegend()

        TaskCompletionChart(stats = stats, mode= mode)
    }
}

@Composable
fun ModeToggle(currentMode: ChartMode, onModeChange: (ChartMode) -> Unit) {

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
                colors = FilterChipDefaults.filterChipColors(
                    // Chip NO seleccionado
                    containerColor = Color.Transparent,
                    labelColor = Color.Gray,

                    // Chip seleccionado
                    selectedContainerColor = MaterialTheme.colorScheme.primary,   // 👈 fondo
                    selectedLabelColor = MaterialTheme.colorScheme.primaryContainer,              // 👈 texto
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = currentMode == mode,
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),          // borde sin seleccionar
                    selectedBorderColor = MaterialTheme.colorScheme.primary,               // borde seleccionado
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
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

    val completionStat = remember(completionStat) {completionStat.toList()}
    val taskList = remember(tasks) { tasks.toList() }

    val beforeTotal = remember(completionStat, taskList) {
        completionStat.count { stat ->
            if (!stat.completed) return@count false
            stat.endDate < stat.proposedDate
        }
    }

    val onTimeTotal = remember(completionStat, taskList) {
        completionStat.count { stat ->
            if (!stat.completed) return@count false
            val deadline = taskList.find{it.id == stat.taskId}?.time?.duration() ?: return@count false
            stat.endDate >= stat.proposedDate && stat.endDate <= stat.proposedDate+deadline
        }
    }

    val lateTotal = remember(completionStat, taskList) {
        completionStat.count { stat ->
            if (!stat.completed) return@count false
            val deadline = taskList.find{it.id == stat.taskId}?.time?.duration() ?: return@count false
            stat.endDate > stat.proposedDate+deadline
        }
    }

    val colors = listOf(beforeColor, onTimeColor, lateColor)

    val columnProvider = remember(colors) {
        val components = colors.map { color ->
            LineComponent(fill = Fill(color), thickness = 40.dp)
        }
        object : ColumnCartesianLayer.ColumnProvider {
            override fun getColumn(
                entry: ColumnCartesianLayerModel.Entry,
                seriesIndex: Int,
                extraStore: ExtraStore
            ) = components[entry.x.toInt()]

            override fun getWidestSeriesColumn(
                seriesIndex: Int,
                extraStore: ExtraStore
            ) = components[seriesIndex]
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
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider = columnProvider,
                    columnCollectionSpacing = 8.dp,
                    mergeMode = { ColumnCartesianLayer.MergeMode.Grouped() }
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
fun HourlyDensityChart(
    completionStat: Sequence<CompletionStat>,
    modifier: Modifier = Modifier
) {
    val fillColor  = Color(0xFF7F77DD)
    val lineColor  = Color(0xFFAFA9EC)
    val axisColor  = Color.White.copy(alpha = 0.4f)
    val gridColor  = Color.White.copy(alpha = 0.10f)
    val labelColor = Color.White.copy(alpha = 0.5f)
    val tz         = TimeZone.currentSystemDefault()
    val textMeasurer = rememberTextMeasurer()

    val labelStyle = TextStyle(
        fontSize  = 10.sp,
        color     = labelColor,
        fontFamily = FontFamily.Default
    )

    val hours: List<Float> = remember(completionStat) {
        completionStat
            .filter { it.completed }
            .map { stat ->
                val ldt = stat.endDate.toLocalDateTime(tz)
                ldt.hour + ldt.minute / 60f
            }
            .toList()
    }

    val countsByHour: IntArray = remember(hours) {
        IntArray(24).also { counts ->
            hours.forEach { h -> counts[h.toInt().coerceIn(0, 23)]++ }
        }
    }
    val maxCount = remember(countsByHour) { countsByHour.max().coerceAtLeast(1) }

    val densityCurve: List<Float> = remember(hours) {
        if (hours.isEmpty()) return@remember List(241) { 0f }
        val n = hours.size.toFloat()
        val mean = hours.sum() / n
        val variance = hours.sumOf { ((it - mean) * (it - mean)).toDouble() } / n
        val sigma = sqrt(variance).toFloat().coerceAtLeast(0.5f)
        val bandwidth = (1.06f * sigma * n.pow(-0.2f)).coerceIn(0.5f, 3f)
        val raw = List(241) { i ->
            val x = i * 24f / 240f
            hours.sumOf { h ->
                val z = (x - h) / bandwidth
                exp(-0.5 * z * z)
            }.toFloat()
        }
        val rawMax = raw.max().coerceAtLeast(1e-6f)
        raw.map { it / rawMax * maxCount }
    }

    val yTicks: List<Int> = remember(maxCount) {
        val step = when {
            maxCount <= 5  -> 1
            maxCount <= 10 -> 2
            maxCount <= 20 -> 5
            else           -> (maxCount / 4).coerceAtLeast(1)
        }
        (0..maxCount step step).toList()
    }

    val yAxisWidth  = 32.dp
    val xAxisHeight = 20.dp

    Surface(
        color = Color(0xFF0D0D0D),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

            Text(
                text = "Productividad por hora del día",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                val yAxisPx = yAxisWidth.toPx()
                val xAxisPx = xAxisHeight.toPx()
                val chartW  = size.width - yAxisPx
                val chartH  = size.height - xAxisHeight.toPx()

                fun xOf(i: Int)     = yAxisPx + i * chartW / 240f
                fun xOfHour(h: Int) = yAxisPx + h * chartW / 24f
                fun yOf(v: Float)   = chartH - (v / maxCount) * chartH * 0.95f

                // ── Eje Y ────────────────────────────────────────────────
                yTicks.forEach { tick ->
                    val y = yOf(tick.toFloat())

                    drawLine(
                        color       = if (tick == 0) axisColor else gridColor,
                        start       = Offset(yAxisPx, y),
                        end         = Offset(size.width, y),
                        strokeWidth = if (tick == 0) 1.dp.toPx() else 0.5.dp.toPx(),
                        pathEffect  = if (tick == 0) null
                        else PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                    )

                    val measured = textMeasurer.measure(tick.toString(), labelStyle)
                    drawText(
                        textLayoutResult = measured,
                        topLeft = Offset(
                            x = yAxisPx - measured.size.width - 4.dp.toPx(),
                            y = y - measured.size.height / 2f
                        )
                    )
                }

                // ── Grid vertical cada 3h ────────────────────────────────
                for (hour in 0..24 step 3) {
                    drawLine(
                        color       = gridColor,
                        start       = Offset(xOfHour(hour), 0f),
                        end         = Offset(xOfHour(hour), chartH),
                        strokeWidth = 0.5.dp.toPx(),
                        pathEffect  = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                    )
                }

                // ── Área rellena ─────────────────────────────────────────
                val fillPath = Path().apply {
                    moveTo(xOf(0), chartH)
                    densityCurve.forEachIndexed { i, d -> lineTo(xOf(i), yOf(d)) }
                    lineTo(xOf(240), chartH)
                    close()
                }
                drawPath(fillPath, color = fillColor.copy(alpha = 0.18f))

                // ── Línea de contorno ────────────────────────────────────
                val strokePath = Path().apply {
                    densityCurve.forEachIndexed { i, d ->
                        if (i == 0) moveTo(xOf(0), yOf(d)) else lineTo(xOf(i), yOf(d))
                    }
                }
                drawPath(
                    strokePath,
                    color = lineColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // ── Eje X ────────────────────────────────────────────────
                val xLabels = listOf("0h","3h","6h","9h","12h","15h","18h","21h","24h")
                xLabels.forEachIndexed { i, label ->
                    val measured = textMeasurer.measure(label, labelStyle)
                    val xCenter  = xOfHour(i * 3)
                    // Alineación: izquierda en 0h, derecha en 24h, centrado el resto
                    val xPos = when (i) {
                        0                -> xCenter
                        xLabels.lastIndex -> xCenter - measured.size.width
                        else             -> xCenter - measured.size.width / 2f
                    }
                    drawText(
                        textLayoutResult = measured,
                        topLeft = Offset(
                            x = xPos,
                            y = chartH + (xAxisPx - measured.size.height) / 2f
                        )
                    )
                }
            }
        }
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