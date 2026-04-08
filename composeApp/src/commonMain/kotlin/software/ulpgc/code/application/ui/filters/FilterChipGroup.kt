package software.ulpgc.code.application.ui.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import software.ulpgc.code.architecture.model.Priority

@Composable
fun FilterChipGroup(
    title: String,
    options: List<String>,
    selectedOptions: Set<String>,
    onSelectionChange: (Set<String>) -> Unit
) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            items(options.size) { index ->
                val option = options[index]

                FilterChip(
                    selected = selectedOptions.contains(option),
                    onClick = {
                        val newSelection =
                            if (selectedOptions.contains(option))
                                selectedOptions - option
                            else
                                selectedOptions + option

                        onSelectionChange(newSelection)
                    },
                    label = { Text(option) },
                )
            }
        }
    }
}