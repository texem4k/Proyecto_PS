package software.ulpgc.code.application.ui

import Screen
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import software.ulpgc.code.application.ui.pages.DialMenuItem
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DialMenu(onNavigate: (Screen) -> Unit) {
    val items = listOf(
        DialMenuItem(
            icon = Icons.Default.Task,
            label = "Nueva tarea",
            color = Color(0xFF534AB7),
            onClick = { onNavigate(Screen.TASKS_CREATE) }
        ),
        DialMenuItem(
            icon = Icons.Default.Folder,
            label = "Nuevo tópico",
            color = Color(0xFF1D9E75),
            onClick = { onNavigate(Screen.TOPIC_CREATE) }
        ),
        DialMenuItem(
            icon = Icons.Default.Label,
            label = "Nuevo tag",
            color = Color(0xFFD85A30),
            onClick = { onNavigate(Screen.TAG_CREATE) }
        ),
    )

    var expanded by remember { mutableStateOf(false) }

    Box(contentAlignment = Alignment.Center) {

        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { expanded = false }
            )
        }

        items.forEachIndexed { index, item ->
            DialChildButton(
                item = item,
                index = index,
                total = items.size,
                radiusDp = 70f,
                visible = expanded,
                onDismiss = { expanded = false }
            )
        }

        val fabRotation by animateFloatAsState(
            targetValue = if (expanded) 45f else 0f,
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
            label = "fab_rotation"
        )

        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (expanded) "Cerrar menú" else "Abrir menú",
                modifier = Modifier.rotate(fabRotation)
            )
        }
    }
}

@Composable
private fun DialChildButton(
    item: DialMenuItem,
    index: Int,
    total: Int,
    radiusDp: Float,
    visible: Boolean,
    onDismiss: () -> Unit
) {
    val angleDeg = when (index) {
        0 -> -180.0
        1 -> -90.0
        else -> 0.0
    }
    val angleRad = angleDeg * PI / 180.0

    val offsetX = (radiusDp * cos(angleRad)).toFloat().dp
    val offsetY = (radiusDp * sin(angleRad)).toFloat().dp

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scale_$index"
    )

    Box(
        modifier = Modifier
            .offset(if (offsetX.value < -10) offsetX + 5.dp else offsetX + 19.dp, offsetY)
            .scale(scale)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(item.color)
                .clickable(enabled = visible) {
                    onDismiss()
                    item.onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        val textOffsetX = when {
            offsetX.value > 10 -> 48.dp
            offsetX.value < -10 -> (-75).dp
            else -> (-15).dp
        }

        val textOffsetY = when {
            offsetY.value < -10 -> (-25).dp
            offsetY.value > 10 -> 45.dp
            else -> 10.dp
        }

        Text(
            text = item.label,
            fontSize = 12.sp,
            color = Color.Black,
            modifier = Modifier
                .offset(textOffsetX, textOffsetY)
                .alpha(scale)
        )
    }
}