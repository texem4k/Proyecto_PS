import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.toArgb
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
import com.patrykandpatrick.vico.multiplatform.common.component.rememberTextComponent

@Composable
fun TaskCompletionChart(stats: List<DayStats>) {
    val proposedColor  = Color(0xFF7C83FD)  // violeta brillante
    val completedColor = Color(0xFF00E5B0)  // verde neón

    val textColor   = Color.White
    val gridColor   = Color.White.copy(alpha = 0.15f)
    val axisColor   = Color.White.copy(alpha = 0.5f)

    if (stats.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Sin datos en este período")
        }
        return
    }

    val proposedModel = remember(stats) {
        CartesianChartModel(
            LineCartesianLayerModel.build {
                series(stats.map { it.proposed.toFloat() })
            }
        )
    }

    val completedModel = remember(stats) {
        CartesianChartModel(
            LineCartesianLayerModel.build {
                series(stats.map { it.completed.toFloat() })
            }
        )
    }

    val model = remember(stats) {
        CartesianChartModel(
            LineCartesianLayerModel.build {
                series(stats.map { it.proposed.toFloat() })
                series(stats.map { it.completed.toFloat() })
            }
        )
    }
    Surface(
        color = Color(0xFF0D0D0D),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            fill = LineCartesianLayer.LineFill.single(
                                Fill(Color(proposedColor.toArgb()))
                            )
                        ),
                        LineCartesianLayer.rememberLine(
                            fill = LineCartesianLayer.LineFill.single(
                                Fill(Color(completedColor.toArgb()))
                            )
                        )
                    )
                ),
                startAxis = VerticalAxis.rememberStart(
                    label = rememberAxisLabelComponent(
                        style = TextStyle(color = textColor, fontSize = 11.sp)
                    ),
                    line = rememberAxisLineComponent(
                        fill = Fill(axisColor)
                    ),
                    tick = rememberAxisTickComponent(
                        fill = Fill(axisColor),
                    ),
                    guideline = rememberAxisGuidelineComponent(
                        fill = Fill(gridColor),
                    ),
                    titleComponent = rememberTextComponent(
                        style = TextStyle(
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    ),
                    title = "Nº de tareas"
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    itemPlacer = HorizontalAxis.ItemPlacer.aligned(spacing = { 8 }),
                    label = rememberAxisLabelComponent(style = TextStyle(color = textColor, fontSize = 10.sp)),
                    line  = rememberAxisLineComponent(fill = Fill(axisColor)),
                    tick  = rememberAxisTickComponent(fill = Fill(axisColor)),
                    guideline = rememberAxisGuidelineComponent(fill = Fill(gridColor)),
                    valueFormatter = { _, value, _ ->
                        stats.getOrNull(value.toInt())?.date?.let {
                            "${it.dayOfMonth}/${it.monthNumber}"
                        } ?: ""
                    }
                ),
            ),
            model = model,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 16.dp)
                .background(Color.Transparent)
        )
    }
}