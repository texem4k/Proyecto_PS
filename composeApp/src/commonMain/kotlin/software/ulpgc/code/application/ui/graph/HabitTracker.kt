package software.ulpgc.code.application.ui.graph

import TaskCompletionChart
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import software.ulpgc.code.architecture.model.tasks.Task

enum class ChartMode { DAILY, WEEKLY }

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
        }
    }

    Column(modifier = modifier) {
        // Toggle selector
        ModeToggle(
            currentMode = mode,
            onModeChange = { mode = it }
        )

        Spacer(Modifier.height(16.dp))

        // Leyenda
        ChartLegend()

        Spacer(Modifier.height(8.dp))

        // Gráfica
        TaskCompletionChart(stats = stats)
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
                    Text(if (mode == ChartMode.DAILY) "Diario" else "Semanal")
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

// Colores diferenciados
val ProposedColor  = Color(0xFF5C6BC0)  // Azul/indigo — tareas propuestas
val CompletedColor = Color(0xFF26A69A)  // Teal — tareas completadas