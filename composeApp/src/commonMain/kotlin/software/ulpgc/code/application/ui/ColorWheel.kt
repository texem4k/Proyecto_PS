package software.ulpgc.code.application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun ColorWheelPicker(
    modifier: Modifier = Modifier,
    wheelSize: Dp = 260.dp,
    onColorSelected: (Color) -> Unit = {}
) {
    var selectedColor by remember { mutableStateOf<Color?>(null) }
    var selectorPosition by remember { mutableStateOf<Offset?>(null) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(wheelSize),
            contentAlignment = Alignment.Center
        ) {
            // Rueda cromática
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            pickColor(offset, size.width.toFloat(), size.height.toFloat())
                                ?.let { (color, pos) ->
                                    selectedColor = color
                                    selectorPosition = pos
                                    onColorSelected(color)
                                }
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            pickColor(change.position, size.width.toFloat(), size.height.toFloat())
                                ?.let { (color, pos) ->
                                    selectedColor = color
                                    selectorPosition = pos
                                    onColorSelected(color)
                                }
                        }
                    }
            ) {
                drawColorWheel()

                // Indicador de selección
                selectorPosition?.let { pos ->
                    drawCircle(
                        color = Color.White,
                        radius = 14.dp.toPx(),
                        center = pos,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    selectedColor?.let { col ->
                        drawCircle(
                            color = col,
                            radius = 11.dp.toPx(),
                            center = pos
                        )
                    }
                }
            }

            // Centro: muestra el color seleccionado
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .then(
                        if (selectedColor != null)
                            Modifier.background(selectedColor!!)
                        else
                            Modifier.background(Color.White.copy(alpha = 0.85f))
                    )
            )
        }
    }
}

// Dibuja la rueda HSL completa con sectores y gradiente de saturación
private fun DrawScope.drawColorWheel() {
    val radius = size.minDimension / 2f
    val innerRadius = radius * 0.38f
    val cx = size.width / 2f
    val cy = size.height / 2f
    val steps = 360
    val rings = 60

    for (i in 0 until steps) {
        val startAngle = (i.toFloat() / steps) * 360f - 90f
        val sweepAngle = 360f / steps + 0.5f
        val hue = i.toFloat()

        for (r in 0..rings) {
            val t = r.toFloat() / rings
            val currentRadius = innerRadius + t * (radius - innerRadius)
            val saturation = t

            drawArc(
                color = Color.hsl(hue = hue, saturation = saturation, lightness = 0.5f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(cx - currentRadius, cy - currentRadius),
                size = Size(currentRadius * 2, currentRadius * 2)
            )
        }
    }

    // Borrar el centro para dejar el hueco
    drawCircle(
        color = Color.Transparent,
        radius = innerRadius,
        center = Offset(cx, cy)
    )
}

// Calcula el color y la posición a partir del toque
private fun pickColor(
    offset: Offset,
    width: Float,
    height: Float
): Pair<Color, Offset>? {
    val cx = width / 2f
    val cy = height / 2f
    val radius = minOf(width, height) / 2f
    val innerRadius = radius * 0.38f

    val dx = offset.x - cx
    val dy = offset.y - cy
    val dist = sqrt(dx * dx + dy * dy)

    if (dist < innerRadius || dist > radius) return null

    var angle = atan2(dy, dx) + (PI / 2f).toFloat()
    if (angle < 0) angle += (2 * PI).toFloat()

    val hue = (angle / (2 * PI)).toFloat() * 360f
    val saturation = ((dist - innerRadius) / (radius - innerRadius))
    val color = Color.hsl(hue = hue, saturation = saturation, lightness = 0.5f)

    return Pair(color, offset)
}


fun Color.toRgbString(): String {
    return "RGB(${(red * 255).toInt()}, ${(green * 255).toInt()}, ${(blue * 255).toInt()})"
}