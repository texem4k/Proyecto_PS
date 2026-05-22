package software.ulpgc.code.application.ui

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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import software.ulpgc.code.application.io.cloudDB.SupabaseAuth
import software.ulpgc.code.application.io.cloudDB.SupabaseAuth.isLoggedIn
import software.ulpgc.code.application.ui.pages.DialMenuItem
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DialMenu(
    onCreateTask: () -> Unit,
    onCreateTopic: () -> Unit,
    onCreateTag: () -> Unit,
    onCreateGroup: () -> Unit
) {
    val authReady = SupabaseAuth.ready.collectAsState()
    val items = buildList {
        add(DialMenuItem(
            icon = Icons.Default.Task,
            label = "Nueva tarea",
            color = Color(0xFF534AB7),
            onClick = onCreateTask
        ))
        add(DialMenuItem(
            icon = Icons.Default.Folder,
            label = "Nuevo tópico",
            color = Color(0xFF1D9E75),
            onClick = onCreateTopic
        ))
        add(DialMenuItem(
            icon = Icons.Default.LocalOffer,
            label = "Nuevo tag",
            color = Color(0xFFD85A30),
            onClick = onCreateTag
        ))
        if (authReady.value && isLoggedIn()) {
            add(DialMenuItem(
                icon = Icons.Default.Group,
                label = "Nuevo grupo",
                color = Color(0xFFC8C804),
                onClick = onCreateGroup
            ))
        }
    }

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
                visible = expanded,
                total = items.size,
                onDismiss = { expanded = false }
            )
        }

        val rotation by animateFloatAsState(
            targetValue = if (expanded) 45f else 0f,
            animationSpec = tween(250, easing = FastOutSlowInEasing),
            label = ""
        )

        FloatingActionButton(
            onClick = { expanded = !expanded },
            shape = CircleShape,
            modifier = Modifier.size(44.dp),
            containerColor = MaterialTheme.colorScheme.primary,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

@Composable
private fun DialChildButton(
    item: DialMenuItem,
    index: Int,
    total: Int,
    visible: Boolean,
    onDismiss: () -> Unit
) {
    val angleDeg = when (total) {
        3 -> when (index) {
            0 -> 180.0
            1 -> 90.0
            2 -> 0.0
            else -> 0.0
        }
        4 -> when (index) {
            0 -> 180.0
            1 -> 115.0
            2 -> 65.0
            3 -> 0.0
            else -> 0.0
        }
        else -> index * (180.0 / maxOf(total - 1, 1))
    }

    val angleRad = angleDeg * PI / 180.0
    val radius = 80f

    val offsetX = (radius * cos(angleRad)).toFloat().dp
    val offsetY = -(radius * sin(angleRad)).toFloat().dp

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
            .offset(
                x = when {
                    offsetX.value < -10 -> offsetX + 5.dp
                    offsetX.value < 10  -> offsetX + 20.dp
                    else                -> offsetX + 25.dp
                },
                y = offsetY
            )
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
            offsetX.value > 20  -> 48.dp
            offsetX.value < -20 -> (-85).dp
            else                -> 48.dp
        }

        val textOffsetY = 10.dp

        Text(
            text = item.label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .offset(textOffsetX, textOffsetY)
                .alpha(scale)
        )
    }
}