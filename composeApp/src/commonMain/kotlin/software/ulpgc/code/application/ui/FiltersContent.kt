package software.ulpgc.code.application.ui


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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FilterContent(
    onApply: (TaskFilters) -> Unit
    //Añadir lista de tópicos
) {
    var tempFilters by remember { mutableStateOf(TaskFilters()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text("Filtros", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        FilterChipGroup(
            title = "Prioridad",
            options = listOf("Urgente", "Alta", "Media", "Baja"),
            selectedOptions = tempFilters.priority,
            onSelectionChange = {
                tempFilters = tempFilters.copy(priority = it)
            }
        )

        FilterChipGroup(
            title = "Tópicos",
            options = listOf("Estudios", "Topico1", "Topico2"),
            selectedOptions = tempFilters.topics, //tempFilters.topics
            onSelectionChange = {
                tempFilters = tempFilters.copy(topics = it)//topics = it
            }
        )


        Spacer(Modifier.height(24.dp))
        if(!tempFilters.priority.isEmpty() || !tempFilters.topics.isEmpty() || !tempFilters.status.isEmpty()) {
            tempFilters.hasFilter = true
        }
        Button(
            onClick = {
                val filtersToApply = tempFilters.copy(
                    hasFilter = tempFilters.priority.isNotEmpty() ||
                            tempFilters.topics.isNotEmpty()  ||
                            tempFilters.status.isNotEmpty()
                )
                onApply(filtersToApply)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Aplicar filtros")
        }
    }
}