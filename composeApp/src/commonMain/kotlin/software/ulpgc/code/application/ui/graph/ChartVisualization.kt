import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.multiplatform.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import software.ulpgc.code.application.ui.graph.DayStats
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.rememberAxisLineComponent
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.rememberAxisTickComponent
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.common.component.rememberTextComponent
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/*
@Composable
fun TaskCompletionChart(stats: List<DayStats>,isWeekly: Boolean = false) {
    val proposedColor = Color(0xFF7C83FD)
    val completedColor = Color(0xFF00E5B0)

    val textColor = Color.White
    val gridColor = Color.White.copy(alpha = 0.15f)
    val axisColor = Color.White.copy(alpha = 0.5f)

    //val sortedStats = remember(stats) { stats.distinctBy { it.date }.sortedBy { it.date } }

    val filledStats = remember(stats, isWeekly) {
        if (stats.isEmpty()) return@remember emptyList<DayStats>()

        // 1. Normalizar: Si es semanal, redondeamos al "lunes" anterior
        val normalized = stats.map {
            val date = if (isWeekly) {
                // Ajuste simple: resta el día de la semana (0=lunes, 6=domingo)
                it.date.minus(it.date.dayOfWeek.ordinal, DateTimeUnit.DAY)
            } else it.date
            it.copy(date = date)
        }.groupBy { it.date }
            .map { (date, list) ->
                DayStats(date, list.sumOf { it.proposed }, list.sumOf { it.completed })
            }.sortedBy { it.date }

        // 2. Rellenar huecos
        val result = mutableListOf<DayStats>()
        var currentDate = normalized.first().date
        val endDate = normalized.last().date
        val statsMap = normalized.associateBy { it.date }

        while (currentDate <= endDate) {
            result.add(statsMap[currentDate] ?: DayStats(currentDate, 0, 0))
            currentDate = if (isWeekly) currentDate.plus(7, DateTimeUnit.DAY)
            else currentDate.plus(1, DateTimeUnit.DAY)
        }
        result
    }
    if (filledStats.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f),
            contentAlignment = Alignment.Center
        ) {
            Text("Sin datos en este período")
        }
        return
    }

    val minDate = filledStats.first().date
    val model = remember(filledStats) {
        val baseEpochDay = minDate.toEpochDays()
        CartesianChartModel(
            LineCartesianLayerModel.build {
                series(
                    x = filledStats.map { (it.date.toEpochDays() - baseEpochDay).toFloat() },
                    y = filledStats.map { it.proposed.toFloat() }
                )
                series(
                    x = filledStats.map { (it.date.toEpochDays() - baseEpochDay).toFloat() },
                    y = filledStats.map { it.completed.toFloat() }
                )
            }
        )
    }

    val xSpacing = remember(filledStats.size) {
        when {
            filledStats.size <= 6 -> 1
            filledStats.size <= 12 -> 2
            filledStats.size <= 24 -> 3
            filledStats.size <= 36 -> 4
            else -> (filledStats.size / 10).coerceAtLeast(1)
        }
    }

    val maxY = remember(filledStats) {
        maxOf(
            filledStats.maxOf { it.proposed },
            filledStats.maxOf { it.completed }
        )
    }

    Surface(
        color = Color(0xFF0D0D0D),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(all = 16.dp)
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            fill = LineCartesianLayer.LineFill.single(
                                Fill(proposedColor)
                            )
                        ),
                        LineCartesianLayer.rememberLine(
                            fill = LineCartesianLayer.LineFill.single(
                                Fill(completedColor)
                            )
                        )
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
                    valueFormatter = { _, value, _ ->
                        value.toInt().toString()
                    }
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    itemPlacer = HorizontalAxis.ItemPlacer.aligned(
                        spacing = { xSpacing }
                    ),
                    label = rememberAxisLabelComponent(
                        style = TextStyle(color = textColor, fontSize = 10.sp)
                    ),
                    line = rememberAxisLineComponent(fill = Fill(axisColor)),
                    tick = rememberAxisTickComponent(fill = Fill(axisColor)),
                    guideline = rememberAxisGuidelineComponent(fill = Fill(gridColor)),
                    valueFormatter = { _, value, _ ->
                        val date = minDate.plus(value.toInt(), if (isWeekly) DateTimeUnit.WEEK else DateTimeUnit.DAY)
                        "${date.dayOfMonth}/${date.monthNumber}"
                        /*
                        sortedStats.getOrNull(value.toInt())?.date?.let {
                            "${it.dayOfMonth}/${it.monthNumber}"
                        } ?: ""

                         */

                    }
                )
            ),
            model = model,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 16.dp)
                .background(Color.Transparent)
        )
    }
}*/
@Composable
fun TaskCompletionChart(
    stats: List<DayStats>,
    isWeekly: Boolean = false
) {
    val proposedColor  = Color(0xFF7C83FD)
    val completedColor = Color(0xFF00E5B0)
    val textColor  = Color.White
    val gridColor  = Color.White.copy(alpha = 0.15f)
    val axisColor  = Color.White.copy(alpha = 0.5f)

    if (stats.isEmpty()) {
        Box(
            modifier         = Modifier.fillMaxWidth().fillMaxHeight(0.9f),
            contentAlignment = Alignment.Center
        ) { Text("Sin datos en este período") }
        return
    }

    // X = índices 0..N-1, espaciado uniforme garantizado
    val model = remember(stats) {
        val xValues = stats.indices.map { it.toFloat() }
        CartesianChartModel(
            LineCartesianLayerModel.build {
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
        color    = Color(0xFF0D0D0D),
        shape    = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(all = 16.dp)
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            fill = LineCartesianLayer.LineFill.single(Fill(proposedColor))
                        ),
                        LineCartesianLayer.rememberLine(
                            fill = LineCartesianLayer.LineFill.single(Fill(completedColor))
                        )
                    )
                ),
                startAxis = VerticalAxis.rememberStart(
                    itemPlacer = VerticalAxis.ItemPlacer.step(step = { 1.0 }),
                    label = rememberAxisLabelComponent(
                        style = TextStyle(color = textColor, fontSize = 11.sp)
                    ),
                    line      = rememberAxisLineComponent(fill = Fill(axisColor)),
                    tick      = rememberAxisTickComponent(fill = Fill(axisColor)),
                    guideline = rememberAxisGuidelineComponent(fill = Fill(gridColor)),
                    titleComponent = rememberTextComponent(
                        style = TextStyle(
                            color      = textColor,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    ),
                    title          = "Nº de tareas",
                    valueFormatter = { _, value, _ -> value.toInt().toString() }
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    itemPlacer     = HorizontalAxis.ItemPlacer.aligned(spacing = { 1 }),
                    label          = rememberAxisLabelComponent(
                        style = TextStyle(color = textColor, fontSize = 10.sp)
                    ),
                    line           = rememberAxisLineComponent(fill = Fill(axisColor)),
                    tick           = rememberAxisTickComponent(fill = Fill(axisColor)),
                    guideline      = rememberAxisGuidelineComponent(fill = Fill(gridColor)),
                    valueFormatter = bottomAxisFormatter
                )
            ),
            model    = model,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 16.dp)
                .background(Color.Transparent)
        )
    }
}