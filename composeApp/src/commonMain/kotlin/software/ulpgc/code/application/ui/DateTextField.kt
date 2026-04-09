package software.ulpgc.code.application.ui

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.*
import software.ulpgc.code.application.ui.pages.formatDate

@Composable
fun DateTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    read: Boolean=false
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
        modifier = modifier,
        readOnly = read,
        colors = TextFieldDefaults.colors(
            // Si es readOnly, usamos Gris; si no, el color normal
            focusedContainerColor = if (read) Color.DarkGray else Color.Unspecified,
            unfocusedContainerColor = if (read) Color.DarkGray else Color.Unspecified,

            // También puedes cambiar el color del texto para que se vea "desactivado"
            focusedTextColor = if (read) Color.DarkGray else Color.Black,
            unfocusedTextColor = if (read) Color.DarkGray else Color.Black,

            // Ocultar la línea indicadora si es de solo lectura
            focusedIndicatorColor = if (read) Color.Transparent else Color.Blue
        ),
    )
}