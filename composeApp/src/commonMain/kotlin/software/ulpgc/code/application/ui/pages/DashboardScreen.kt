package software.ulpgc.code.application.ui.pages

import Screen
import software.ulpgc.code.application.ui.UpcomingTasksPanel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.application.ui.SideBar
import software.ulpgc.code.application.ui.graph.HabitTrackerChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigate: (Screen) -> Unit,
    store: Storage,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onDeleted: () -> Unit = {},
    onSettingsClick: () -> Unit={}
) {
    Row(modifier = Modifier.fillMaxSize()) {

        SideBar(
            selectedScreen = Screen.DASHBOARD,
            onNavigate = onNavigate,
            onSettingsClick = onSettingsClick
        )

        Column(
            modifier = Modifier
                .weight(2.7f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            // ── Mitad superior: dividida en 2 columnas ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 8.dp)
            ) {
                // Celda superior izquierda
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Panel izquierdo")
                    }
                }

                // Celda superior derecha
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Panel derecho")
                    }
                }
            }

            // ── Mitad inferior: gráfica a todo el ancho ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                val tasks = remember(store) { store.tasks().toList() }
                HabitTrackerChart(tasks = tasks)
            }
        }
    }
}