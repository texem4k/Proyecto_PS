package software.ulpgc.code.application

/*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*


@Composable
fun ColorWheelPickerScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        ColorWheelPicker()
    }
}

// ─────────────────────────────────────────────
//  Componente principal
// ─────────────────────────────────────────────
@Composable
fun ColorWheelPicker(
    wheelSize: Dp = 280.dp,
    onColorChanged: ((Color) -> Unit)? = null
) {
    // Estado: ángulo (hue 0-360) y distancia normalizada (sat 0-1)
    var hue by remember { mutableStateOf(0f) }
    var saturation by remember { mutableStateOf(1f) }
    val brightness = 1f  // fijo a máximo para ver todos los colores

    val selectedColor by remember(hue, saturation) {
        derivedStateOf { hsvToColor(hue, saturation, brightness) }
    }

    LaunchedEffect(selectedColor) {
        onColorChanged?.invoke(selectedColor)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.padding(24.dp)
    ) {
        // ── Rueda ──
        ColorWheel(
            size = wheelSize,
            hue = hue,
            saturation = saturation,
            onPickColor = { newHue, newSat ->
                hue = newHue
                saturation = newSat
            }
        )

        // ── Recuadro de color + valores RGB ──
        ColorPreviewBox(color = selectedColor)
    }
}

// ─────────────────────────────────────────────
//  Rueda de color con puntero
// ─────────────────────────────────────────────
@Composable
private fun ColorWheel(
    size: Dp,
    hue: Float,
    saturation: Float,
    onPickColor: (hue: Float, sat: Float) -> Unit
) {
    // Bitmap de la rueda: se recalcula solo cuando cambia el tamaño
    val wheelBitmap = remember(size) { mutableStateOf<ImageBitmap?>(null) }

    // Posición del puntero sobre la rueda
    val pointerAngleRad = Math.toRadians(hue.toDouble()).toFloat()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val (h, s) = offsetToHueSat(offset, size.toPx() / 2f)
                        onPickColor(h, s)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val (h, s) = offsetToHueSat(change.position, size.toPx() / 2f)
                        onPickColor(h, s)
                    }
                }
        ) {
            val radius = this.size.minDimension / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // Dibujar la rueda pixel a pixel (o usar el bitmap cacheado)
            if (wheelBitmap.value == null) {
                wheelBitmap.value = buildWheelBitmap(radius.toInt() * 2)
            }
            wheelBitmap.value?.let { bmp ->
                drawImage(bmp)
            }

            // Borde exterior sutil
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = radius,
                style = Stroke(width = 2f)
            )

            // ── Puntero (crosshair circular típico) ──
            val px = center.x + saturation * radius * cos(pointerAngleRad)
            val py = center.y + saturation * radius * sin(pointerAngleRad)
            val pointerPos = Offset(px, py)
            val pointerRadius = 12f

            // Sombra
            drawCircle(
                color = Color.Black.copy(alpha = 0.35f),
                radius = pointerRadius + 2f,
                center = pointerPos + Offset(1f, 1.5f)
            )
            // Relleno blanco
            drawCircle(
                color = Color.White,
                radius = pointerRadius,
                center = pointerPos
            )
            // Borde oscuro
            drawCircle(
                color = Color.Black.copy(alpha = 0.5f),
                radius = pointerRadius,
                center = pointerPos,
                style = Stroke(width = 1.5f)
            )
            // Cruz interior
            val crossLen = pointerRadius * 0.55f
            val crossColor = Color.Black.copy(alpha = 0.65f)
            drawLine(crossColor, pointerPos - Offset(crossLen, 0f), pointerPos + Offset(crossLen, 0f), 1.5f)
            drawLine(crossColor, pointerPos - Offset(0f, crossLen), pointerPos + Offset(0f, crossLen), 1.5f)
        }
    }
}

// ─────────────────────────────────────────────
//  Recuadro de vista previa del color
// ─────────────────────────────────────────────
@Composable
private fun ColorPreviewBox(color: Color) {
    val r = (color.red * 255).roundToInt()
    val g = (color.green * 255).roundToInt()
    val b = (color.blue * 255).roundToInt()
    val hex = "#%02X%02X%02X".format(r, g, b)

    // Determina si el texto debe ser claro u oscuro según la luminancia
    val luminance = 0.299f * color.red + 0.587f * color.green + 0.114f * color.blue
    val textColor = if (luminance > 0.5f) Color(0xFF1A1A1A) else Color(0xFFF5F5F5)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .padding(vertical = 20.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Valor HEX grande
        Text(
            text = hex,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = textColor,
            letterSpacing = 2.sp
        )

        // Valores RGB separados
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RgbChip(label = "R", value = r, textColor = textColor)
            RgbChip(label = "G", value = g, textColor = textColor)
            RgbChip(label = "B", value = b, textColor = textColor)
        }
    }
}

// ─────────────────────────────────────────────
//  Chip individual R / G / B
// ─────────────────────────────────────────────
@Composable
private fun RgbChip(label: String, value: Int, textColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textColor.copy(alpha = 0.6f),
            letterSpacing = 1.sp
        )
        Text(
            text = value.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = textColor
        )
    }
}

// ─────────────────────────────────────────────
//  Genera el bitmap de la rueda HSV completa
// ─────────────────────────────────────────────
private fun buildWheelBitmap(diameter: Int): ImageBitmap {
    val pixels = IntArray(diameter * diameter)
    val center = diameter / 2f
    val radius = center

    for (y in 0 until diameter) {
        for (x in 0 until diameter) {
            val dx = x - center
            val dy = y - center
            val dist = sqrt(dx * dx + dy * dy)

            if (dist > radius) {
                pixels[y * diameter + x] = 0  // transparente fuera del círculo
                continue
            }

            val angle = (Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())) + 360) % 360
            val sat = (dist / radius).toFloat().coerceIn(0f, 1f)
            val color = hsvToColor(angle.toFloat(), sat, 1f)
            pixels[y * diameter + x] = color.toArgb()
        }
    }

    val bmp = ImageBitmap(diameter, diameter)
    val canvas = Canvas(bmp)
    val paint = Paint().apply { isAntiAlias = false }

    // Dibujamos los píxeles usando un bitmap de Android
    val androidBitmap = android.graphics.Bitmap.createBitmap(
        pixels, diameter, diameter, android.graphics.Bitmap.Config.ARGB_8888
    )
    canvas.nativeCanvas.drawBitmap(androidBitmap, 0f, 0f, null)
    return bmp
}

// ─────────────────────────────────────────────
//  Convierte offset de pantalla a (hue, sat)
// ─────────────────────────────────────────────
private fun offsetToHueSat(offset: Offset, radius: Float): Pair<Float, Float> {
    val dx = offset.x - radius
    val dy = offset.y - radius
    val dist = sqrt(dx * dx + dy * dy)
    val hue = ((Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())) + 360) % 360).toFloat()
    val sat = (dist / radius).coerceIn(0f, 1f)
    return Pair(hue, sat)
}

// ─────────────────────────────────────────────
//  HSV → Color de Compose
// ─────────────────────────────────────────────
private fun hsvToColor(hue: Float, saturation: Float, value: Float): Color {
    val h = hue / 60f
    val i = h.toInt() % 6
    val f = h - h.toInt()
    val p = value * (1f - saturation)
    val q = value * (1f - f * saturation)
    val t = value * (1f - (1f - f) * saturation)
    return when (i) {
        0 -> Color(value, t, p)
        1 -> Color(q, value, p)
        2 -> Color(p, value, t)
        3 -> Color(p, q, value)
        4 -> Color(t, p, value)
        5 -> Color(value, p, q)
        else -> Color.White
    }
}

 */