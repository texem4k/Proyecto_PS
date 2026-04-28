package software.ulpgc.code.application.ui.filters

import Screen
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.Priority

@Composable
fun FilterContent(
    onApply: (TaskFilters) -> Unit,
    store: Storage,
    onNavigate: (Screen) -> Unit,
    onDismiss: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var tempFilters by remember { mutableStateOf(TaskFilters()) }

    var showCreateTopic by remember { mutableStateOf(false) }
    var showCreateTag by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text("Filtros", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        FilterChipGroup(
            title = "Prioridad",
            options = Priority.entries.map { it.text },
            selectedOptions = tempFilters.priority,
            onSelectionChange = {
                tempFilters = tempFilters.copy(priority = it)
            }
        )

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tópicos", modifier = Modifier.weight(1f))
        }

        FilterChipGroup(
            title = "",
            options = store.topics().toList().map { it.name },
            selectedOptions = tempFilters.topics,
            onSelectionChange = {
                tempFilters = tempFilters.copy(topics = it)
            }
        )

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tags", modifier = Modifier.weight(1f))
        }

        FilterChipGroup(
            title = "",
            options = store.tags().toList().map { it.name },
            selectedOptions = tempFilters.tags,
            onSelectionChange = {
                tempFilters = tempFilters.copy(tags = it)
            }
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val filtersToApply = tempFilters.copy(
                    hasFilter = tempFilters.priority.isNotEmpty() ||
                            tempFilters.topics.isNotEmpty() ||
                            tempFilters.tags.isNotEmpty()
                )
                onApply(filtersToApply)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Aplicar filtros")
        }
    }

    // 🪟 POPUPS
    if (showCreateTopic) {
        CreateTopicDialog(
            store = store,
            onClose = { showCreateTopic = false }
        )
    }

    if (showCreateTag) {
        CreateTagDialog(
            store = store,
            onClose = { showCreateTag = false }
        )
    }
}