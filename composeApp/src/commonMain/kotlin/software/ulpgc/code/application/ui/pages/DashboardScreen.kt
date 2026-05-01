package software.ulpgc.code.application.ui.pages

import Screen
import UpcomingTasksPanel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import software.ulpgc.code.architecture.io.Storage
import androidx.compose.ui.unit.sp
import software.ulpgc.code.application.ui.SideBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigate: (Screen) -> Unit,
    store: Storage,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onDeleted: () -> Unit = {}

) {


    Box(
        modifier = Modifier.fillMaxSize()
        ){

        Row(modifier = Modifier.fillMaxSize()) {

            SideBar(selectedScreen = Screen.DASHBOARD,
                onNavigate = onNavigate,
            )

            /*
            Column(
                modifier = Modifier
                    .weight(2.7f)
                    .fillMaxHeight()
                    .padding(16.dp)

            ) {
            */

                /*
                // Fila superior: dos widgets lado a lado
                Row(modifier = Modifier.fillMaxWidth().weight(0.35f),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Top) {
                    SearchBar(
                        text = searchText,
                        onTextChange = onSearchTextChange,
                        onSearch = { onNavigate(Screen.RESULTS) })
                }
                 */

                Column(modifier = Modifier.fillMaxWidth(0.6f).weight(0.40f).padding(start = 52.dp),) {
                    Card(modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f)
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
                }
                Row(modifier = Modifier.fillMaxWidth().weight(0.55f),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center ) {
                    val group = store.tasks().groupBy { it.topicId }
                    val items = group.entries.toList().take(2)
                    items.forEach { (titulo, tareasGrupo) ->
                        val topicName = store.topics().find { it.id == titulo }?.name ?: "Sin tópico"
                        UpcomingTasksPanel(store, tareasGrupo, topicName, screen = Screen.DASHBOARD)
                    }

                }

            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Card(modifier = Modifier.weight(1f).padding(8.dp, ), shape = RoundedCornerShape(0.dp)) {

                        Text("Widget C")
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Card(modifier = Modifier.weight(1f).padding(8.dp)) {
                        Text("Widget E")
                    }
                }
            }
        }
    }
}