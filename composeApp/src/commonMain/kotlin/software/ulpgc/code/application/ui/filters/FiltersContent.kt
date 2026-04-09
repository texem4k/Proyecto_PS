package software.ulpgc.code.application.ui.filters


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import software.ulpgc.code.application.ui.Screen
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


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text("Filtros", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))
        val prior = mutableListOf<String>()
        Priority.entries.forEach { entry ->
            prior.add(entry.text)
        }
        FilterChipGroup(
            title = "Prioridad",
            options = prior,
            selectedOptions = tempFilters.priority,
            onSelectionChange = {
                tempFilters = tempFilters.copy(priority = it)
            },
            store,
            onNavigate = onNavigate,
            onDismiss = onDismiss
        )
        Spacer(Modifier.height(16.dp))

        val topics = mutableListOf<String>()
        store.topics().forEach {
            topics.add(it.name)
        }
        FilterChipGroup(
            title = "Tópicos",
            options =topics,
            selectedOptions = tempFilters.topics, //tempFilters.topics
            onSelectionChange = {
                tempFilters = tempFilters.copy(topics = it)//topics = it
            },
            store = store,
            onNavigate = onNavigate,
            onDismiss = onDismiss
        )
        Spacer(Modifier.height(16.dp))
        val tags = mutableListOf<String>()
        store.tags().forEach {
            tags.add(it.name)
        }

        FilterChipGroup(
            title = "Tags",
            options =tags,
            selectedOptions = tempFilters.tags, //tempFilters.topics
            onSelectionChange = {
                tempFilters = tempFilters.copy(tags = it)//topics = it
            },
            store = store,
            onNavigate = onNavigate,
            onDismiss = onDismiss
        )


        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                val filtersToApply = tempFilters.copy(
                    hasFilter = tempFilters.priority.isNotEmpty() ||
                            tempFilters.topics.isNotEmpty()  ||
                            tempFilters.status.isNotEmpty() ||
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