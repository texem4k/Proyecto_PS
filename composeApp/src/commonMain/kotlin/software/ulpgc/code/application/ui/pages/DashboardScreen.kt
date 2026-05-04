package software.ulpgc.code.application.ui.pages

import Screen
import software.ulpgc.code.application.ui.UpcomingTasksPanel
import androidx.compose.foundation.layout.Arrangement
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
    onDeleted: () -> Unit = {}
) {
    Row(modifier = Modifier.fillMaxSize()) {

        SideBar(
            selectedScreen = Screen.DASHBOARD,
            onNavigate = onNavigate,
        )

        Column(
            modifier = Modifier
                .weight(2.7f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().weight(0.4f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Widget 1",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp, top = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().weight(0.6f),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val group = store.tasks().groupBy { it.topicId }
                val items = group.entries.toList().take(2)
                items.forEach { (titulo, tareasGrupo) ->
                    val topicName = store.topics().find { it.id == titulo }?.name ?: "Sin tópico"
                    UpcomingTasksPanel(
                        store = store,
                        tareas = tareasGrupo,
                        title = topicName,
                        screen = Screen.DASHBOARD
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp)
            ) {
                Text("Widget E")
            }

            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp),
                shape = RoundedCornerShape(30.dp)
            ) {
                val tasks = remember(store) { store.tasks().toList() }
                HabitTrackerChart(tasks = tasks)
            }
        }
    }
}