package software.ulpgc.code.application.ui.pages

import Screen
import software.ulpgc.code.application.ui.UpcomingTasksPanel
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.Priority
import software.ulpgc.code.architecture.model.tasks.Task



@Composable
fun SearchResultsDialog(
    onDismiss: () -> Unit,
    store: Store,
    value: String,
    onSearchTextChange: (String) -> Unit,
    filters: TaskFilters
) {

    val background = MaterialTheme.colorScheme.surface
    val surface = MaterialTheme.colorScheme.surfaceVariant
    val border = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

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
                accumulated += store.tasks().filter { task -> priority.value == task.priority.value }
            }

            filters.tags.forEach { t ->
                tagsList.add(t)
                val tagId = store.tags().first { it.name == t }.id
                accumulated += store.tasks().filter { task ->
                    task.tags.any { it.toString() == tagId.toString() }
                }
            }

            accumulated.toList()
        } else {
            store.tasks().toList()
                .filter {
                    it.name.normalizeAccents()
                        .contains(value.normalizeAccents(), ignoreCase = true)
                }
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
                .background(background, RoundedCornerShape(20.dp))
                .border(1.dp, border, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (filters.hasFilter) "Filtrado de tareas" else "Resultados de \"$value\"",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
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
                            tint = textSecondary
                        )
                    }
                }

                if (filters.hasFilter) {
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (topicsList.isNotEmpty()) FilterChipRow(
                            "Tópicos",
                            topicsList,
                            surface,
                            border,
                            textPrimary,
                            textSecondary
                        )
                        if (priorityList.isNotEmpty()) FilterChipRow(
                            "Prioridad",
                            priorityList,
                            surface,
                            border,
                            textPrimary,
                            textSecondary
                        )
                        if (tagsList.isNotEmpty()) FilterChipRow(
                            "Tags",
                            tagsList,
                            surface,
                            border,
                            textPrimary,
                            textSecondary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = border)
                Spacer(Modifier.height(12.dp))

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
                        modifier = Modifier.weight(1f).fillMaxWidth(),
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
                                color = textSecondary,
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

fun String.normalizeAccents(): String {
    return this
        .replace('á', 'a').replace('Á', 'A')
        .replace('é', 'e').replace('É', 'E')
        .replace('í', 'i').replace('Í', 'I')
        .replace('ó', 'o').replace('Ó', 'O')
        .replace('ú', 'u').replace('Ú', 'U')
        .replace('ü', 'u').replace('Ü', 'U')
        .replace('ñ', 'n').replace('Ñ', 'N')
}