package software.ulpgc.code.application.ui

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import software.ulpgc.code.application.ui.pages.formatDate

@Composable
fun DateTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String
) {
    TextField(
        value = value,
        onValueChange = { input ->
            val digits = input.filter { it.isDigit() }
            val trimmed = digits.take(8)
            onValueChange(formatDate(trimmed))
        },
        label = { Text(label) },
        placeholder = { Text("dd/mm/aaaa") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = DateVisualTransformation(),
        singleLine = true,
        modifier = modifier
    )
}