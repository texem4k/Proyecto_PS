package software.ulpgc.code.application.ui.pages

import Screen
import UpcomingTasksPanel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import software.ulpgc.code.application.ui.filters.TaskFilters
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.Priority
import software.ulpgc.code.architecture.model.tasks.Task

/*
@Composable
fun SearchTaskScreen(
    onNavigate: (Screen) -> Unit,
    store: Storage,
    value: String,
    onSearchTextChange: (String) -> Unit,
    filters: TaskFilters
) {
    val topicsList = mutableListOf<String>()
    val priorityList = mutableListOf<String>()
    val tagsList = mutableListOf<String>()
    var expand=true

    var search: List<Task> = listOf()
    if (filters.hasFilter) {

        filters.topics.forEach { topicFilter ->
            val topicId = store.topics().filter { it.name == topicFilter }.first().id
            topicsList.add(topicFilter)
            val temp = store.tasks().filter { it.topicId == topicId }.toList()
            search = temp + search.toMutableList()
        }

        for (f in filters.priority) {
            priorityList.add(f)
            val priority = Priority.entries.first { p -> p.text == f }
            val temp = store.tasks().filter { task -> priority.values.contains(task.priority) }.toList()
            search = temp + search.toMutableList()
        }

        filters.tags.forEach { t ->
            tagsList.add(t)
            val tagId = store.tags().filter { it.name == t }.first().id
            val tagIdStr = tagId.toString() // 👈 normaliza a String
            val temp = store.tasks().filter { task ->
                task.tags.any { it.toString() == tagIdStr } // 👈 compara como String
            }.toList()
            search = temp + search.toMutableList()
        }
    } else {
        search = store.tasks().filter { it.name.contains(value, true) }.toList()
    }


    Dialog(
        onDismissRequest = {expand=false},
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ){
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {

            if (search.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(0.3f).padding(16.dp)) {
                    if (filters.hasFilter) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Text("Resultado mediante filtrado", modifier = Modifier.padding(bottom = 16.dp))
                            if (topicsList.isNotEmpty()) Text("Tópicos: ${topicsList.joinToString(", ")}", modifier = Modifier.padding(bottom = 8.dp))
                            if (priorityList.isNotEmpty()) Text("Prioridad: ${priorityList.joinToString(", ")}", modifier = Modifier.padding(bottom = 8.dp))
                            if (tagsList.isNotEmpty()) Text("Tags: ${tagsList.joinToString(", ")}", modifier = Modifier.padding(bottom = 8.dp))
                        }
                    } else {
                        Text("Resultado de $value", modifier = Modifier.align(Alignment.Center))
                    }
                    Button(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        onClick = { onSearchTextChange(""); filters.hasFilter=false;onNavigate(Screen.TASKS) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) { Text("✖\uFE0E") }
                }

                Box(modifier = Modifier.weight(1f).padding(top=32.dp)) {
                    UpcomingTasksPanel(store, search, title = "Resultados", screen = Screen.RESULTS)
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No ha habido coincidencia con $value")
                        Button(onClick = { onSearchTextChange(""); filters.hasFilter=false;onNavigate(Screen.TASKS) }) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        }
    }
}
*/

@Composable
fun SearchResultsDialog(
    onDismiss: () -> Unit,
    onNavigate: (Screen) -> Unit,
    store: Storage,
    value: String,
    onSearchTextChange: (String) -> Unit,
    filters: TaskFilters
) {

    val creamBackground = Color(0xFFFDF6E3)
    val creamSurface = Color(0xFFF5ECD7)
    val creamBorder = Color(0xFFD4B896)
    val textBrown = Color(0xFF5C4033)
    val textBrownLight = Color(0xFF8D6E63)

    val topicsList = remember { mutableListOf<String>() }
    val priorityList = remember { mutableListOf<String>() }
    val tagsList = remember { mutableListOf<String>() }

    val search: List<Task> = remember(filters, value) {
        if (filters.hasFilter) {
            val accumulated = LinkedHashSet<Task>()

            filters.topics.forEach { topicFilter ->
                topicsList.add(topicFilter)
                val topicId = store.topics().first { it.name == topicFilter }.id
                accumulated += store.tasks().filter { it.topicId == topicId }
            }

            filters.priority.forEach { f ->
                priorityList.add(f)
                val priority = Priority.entries.first { p -> p.text == f }
                accumulated += store.tasks().filter { task -> priority.values.contains(task.priority) }
            }

            filters.tags.forEach { t ->
                tagsList.add(t)
                val tagId = store.tags().first { it.name == t }.id
                val tagIdStr = tagId.toString()
                accumulated += store.tasks().filter { task ->
                    task.tags.any { it.toString() == tagIdStr }
                }
            }

            accumulated.toList()
        } else {
            store.tasks()
                .filter { it.name.contains(value, ignoreCase = true) }
                .toList() // 👈 esto era lo que faltaba, sin el cast
        }
    }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.40f)
                .fillMaxHeight(0.70f)
                .background(creamBackground, RoundedCornerShape(20.dp))
                .border(1.dp, creamBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {

                // ── Header ──────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (filters.hasFilter) "Filtrado de tareas" else "Resultados de \"$value\"",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textBrown
                    )
                    IconButton(
                        onClick = {
                            onSearchTextChange("")
                            filters.hasFilter = false
                            onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = textBrownLight
                        )
                    }
                }

                // ── Chips de filtros activos ─────────────────────────────
                if (filters.hasFilter) {
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (topicsList.isNotEmpty()) FilterChipRow("Tópicos", topicsList, creamSurface, creamBorder, textBrown, textBrownLight)
                        if (priorityList.isNotEmpty()) FilterChipRow("Prioridad", priorityList, creamSurface, creamBorder, textBrown, textBrownLight)
                        if (tagsList.isNotEmpty()) FilterChipRow("Tags", tagsList, creamSurface, creamBorder, textBrown, textBrownLight)
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = creamBorder)
                Spacer(Modifier.height(12.dp))

                // ── Contenido ────────────────────────────────────────────
                if (search.isNotEmpty()) {
                    Box(modifier = Modifier.weight(2f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                        UpcomingTasksPanel(
                            store = store,
                            tareas = search,
                            title = "Resultados (${search.size})",
                            screen = Screen.RESULTS,
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "😔",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Text(
                                text = "Sin resultados para \"$value\"",
                                style = MaterialTheme.typography.bodyLarge,
                                color = textBrownLight,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipRow(
    label: String,
    items: List<String>,
    surface: Color,
    border: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelSmall,
            color = textSecondary,
            fontWeight = FontWeight.SemiBold
        )
        items.forEach { item ->
            Box(
                modifier = Modifier
                    .background(surface, RoundedCornerShape(50))
                    .border(1.dp, border, RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = item,
                    style = MaterialTheme.typography.labelSmall,
                    color = textPrimary
                )
            }
        }
    }
}