package software.ulpgc.code.application.ui.filters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.Priority

@Composable
fun FilterContent(
    onApply: (TaskFilters) -> Unit,
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
        FilterPanel(
            type = "Prioridad",
            filters = tempFilters,
            options = Priority.entries.map { it.text }.distinct(),
            selected = tempFilters.priority,
            onFilterChange = { tempFilters = it }
        )

        Spacer(Modifier.height(16.dp))

        FilterPanel(
            type = "Tópicos",
            filters = tempFilters,
            options = Store.topics().toList().map { it.name },
            selected = tempFilters.topics,
            onFilterChange = { tempFilters = it }
        )

        Spacer(Modifier.height(16.dp))

        FilterPanel(
            type = "Tags",
            filters = tempFilters,
            options = Store.tags().toList().map { it.name },
            selected = tempFilters.tags,
            onFilterChange = { tempFilters = it }
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
}


@Composable
fun FilterPanel(
    type: String,
    filters: TaskFilters,
    options: List<String>,
    selected: Set<String>,
    onFilterChange: (TaskFilters) -> Unit
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