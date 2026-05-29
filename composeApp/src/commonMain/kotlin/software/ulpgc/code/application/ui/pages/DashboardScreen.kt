package software.ulpgc.code.application.ui.pages

import Screen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import software.ulpgc.code.application.ui.widgets.KpiDashboard
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.application.ui.widgets.SideBar
import software.ulpgc.code.application.ui.graph.BarGraph
import software.ulpgc.code.application.ui.graph.HabitTrackerChart
import software.ulpgc.code.application.ui.graph.HourlyDensityChart
import software.ulpgc.code.application.ui.widgets.rememberUserKpi

enum class StatMode(val displayName: String) {
    General("General"),
    ActualGroup("Grupo actual")
}

enum class TypeChart {BarChart, DotChart}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigate: (Screen) -> Unit,
    onSettingsClick: () -> Unit = {},

) {

    var version by remember { mutableStateOf(0) }
    val onDeleted: () -> Unit = { version++ }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        SideBar(
            onNavigate = onNavigate,
            selectedScreen = Screen.DASHBOARD,
            onSettingsClick = onSettingsClick,
            onRefresh = {version++},
            version = version
        )

        var modeToggle by remember { mutableStateOf(StatMode.ActualGroup)}
        var expanded by remember { mutableStateOf(false) }
        val taskFlag = Store.tasks()

        var completionStat by remember(modeToggle) {
            val initial = when (modeToggle) {
                StatMode.ActualGroup -> {
                    val group = Store.tasks().map { it.id }.toHashSet()
                    Store.completions().filter { it.taskId in group }
                }
                StatMode.General -> Store.completions()
            }
            mutableStateOf(initial)
        }

        Column(
            modifier = Modifier
                .weight(2.7f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 8.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {

                    Box(
                        modifier = Modifier.padding(start = 30.dp, top = 10.dp, bottom = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            modifier = Modifier.width(140.dp).align(Alignment.TopStart),
                            onClick = { expanded = true }
                        ) {
                            Text(text = modeToggle.displayName, color = MaterialTheme.colorScheme.primaryContainer)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            offset = DpOffset(
                                x = 15.dp,
                                y = 0.dp
                            ),
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            StatMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Text(mode.displayName)
                                    },
                                    onClick = {
                                        modeToggle = mode
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }


                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 16.dp, bottom = 16.dp, end = 16.dp, top = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val kpi by rememberUserKpi(taskStat = completionStat)
                        KpiDashboard(kpi = kpi)
                    }
                }

                //AQUI
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        var currentMode by remember { mutableStateOf(TypeChart.BarChart) }
                        Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp), horizontalArrangement = Arrangement.Center) {
                            TypeChart.entries.forEach { mode ->
                                FilterChip(
                                    selected = currentMode == mode,
                                    onClick = { currentMode = mode },
                                    label = {
                                        Text(
                                            when (mode) {
                                                TypeChart.BarChart -> "Diagrama de Barras"
                                                TypeChart.DotChart -> "Diagrama de Densidad"
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

                        when (currentMode) {
                            TypeChart.BarChart -> BarGraph(completionStat, taskFlag, Modifier.weight(1f).padding(end = 8.dp))
                            TypeChart.DotChart -> HourlyDensityChart(completionStat, Modifier.weight(1f).padding(8.dp))
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    HabitTrackerChart(tasks = taskFlag, taskStat = completionStat)
                }
            }
        }
    }
}