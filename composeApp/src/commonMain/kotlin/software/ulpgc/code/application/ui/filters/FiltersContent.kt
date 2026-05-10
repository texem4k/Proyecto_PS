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
import kotlin.collections.emptySet

@Composable
fun FilterContent(
    onApply: (TaskFilters) -> Unit,
    store: Storage,
    onDismiss: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var tempFilters by remember { mutableStateOf(TaskFilters()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text("Filtros", style = MaterialTheme.typography.titleLarge)

        /*
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tópicos", modifier = Modifier.weight(1f))
        }

        FilterChipGroup(
            options = Priority.entries.map { it.text },
            selectedOptions = tempFilters.priority,
            onSelectionChange = {
                tempFilters = tempFilters.copy(priority = it)
            }
        )

         */
        FilterPanel(
            type = "Prioridad",
            filters = tempFilters,
            options = Priority.entries.map { it.text },
            selected = tempFilters.priority,
            onFilterChange = { tempFilters = it }
        )

        Spacer(Modifier.height(16.dp))

        /*
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tópicos", modifier = Modifier.weight(1f))
        }

        FilterChipGroup(
            options = store.topics().toList().map { it.name },
            selectedOptions = tempFilters.topics,
            onSelectionChange = {
                tempFilters = tempFilters.copy(topics = it)
            }
        )

         */

        FilterPanel(
            type = "Tópicos",
            filters = tempFilters,
            options = store.topics().toList().map { it.name },
            selected = tempFilters.topics,
            onFilterChange = { tempFilters = it }
        )

        Spacer(Modifier.height(16.dp))

        FilterPanel(
            type = "Tags",
            filters = tempFilters,
            options = store.tags().toList().map { it.name },
            selected = tempFilters.tags,
            onFilterChange = { tempFilters = it }
        )
        /*
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tags", modifier = Modifier.weight(1f))
        }

        FilterChipGroup(
            options = store.tags().toList().map { it.name },
            selectedOptions = tempFilters.tags,
            onSelectionChange = {
                tempFilters = tempFilters.copy(tags = it)
            }
        )

*/

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
}


@Composable
fun FilterPanel(
    type: String,
    filters: TaskFilters,
    options: List<String>,
    selected: Set<String>,
    onFilterChange: (TaskFilters) -> Unit  // ← callback al padre
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(type, modifier = Modifier.weight(1f))
    }

    FilterChipGroup(
        options = options,
        selectedOptions = selected,
        onSelectionChange = { newSelection ->
            val updated = when (type) {
                "Prioridad" -> filters.copy(priority = newSelection)
                "Tópicos"   -> filters.copy(topics = newSelection)
                "Tags"      -> filters.copy(tags = newSelection)
                else        -> filters
            }
            onFilterChange(updated)
        }
    )
}