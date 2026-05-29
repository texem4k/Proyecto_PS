package software.ulpgc.code.application.ui.dataStructure
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlin.time.Instant
import kotlinx.datetime.TimeZone


@Composable
fun PickerField(
    value: String,
    label: String,
    placeholder: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label, color = MaterialTheme.colorScheme.onPrimaryContainer) },
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onPrimaryContainer) },
            trailingIcon = { Icon(icon, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onClick() }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String, Instant) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    val displayText = if (value.matches(Regex("\\d{8}"))) {
        "${value.take(2)}/${value.substring(2, 4)}/${value.drop(4)}"
    } else ""

    PickerField(
        value = displayText,
        label = label,
        placeholder = "Seleccionar fecha",
        icon = Icons.Default.CalendarToday,
        onClick = { showPicker = true },
        modifier = modifier.padding(bottom = 16.dp)
    )

    if (showPicker) {
        val state = rememberDatePickerState()

        DatePickerDialog(
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        val tz = TimeZone.currentSystemDefault()
                        onValueChange(instant.toFormattedDate(tz), instant)
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = state, colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                headlineContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                weekdayContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                dayContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                todayContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                todayDateBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                yearContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                navigationContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                subheadContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                dateTextFieldColors = OutlinedTextFieldDefaults.colors(
                    errorTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    focusedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            ))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    type: String,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    val parts = value.split(':')
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0

    PickerField(
        value = value,
        label = "* Hora de $type",
        placeholder = "hh:mm",
        icon = Icons.Default.Schedule,
        onClick = { showPicker = true },
        modifier = modifier.padding(bottom = 16.dp)
    )

    if (showPicker) {
        val state = rememberTimePickerState(
            initialHour = h,
            initialMinute = m,
            is24Hour = true
        )

        AlertDialog(
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val hour = state.hour.toString().padStart(2, '0')
                    val min = state.minute.toString().padStart(2, '0')
                    onValueChange("$hour:$min")
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancelar")
                }
            },
            text = { TimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    clockDialColor = MaterialTheme.colorScheme.secondary,        // 👈 fondo del reloj
                    clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    clockDialUnselectedContentColor = MaterialTheme.colorScheme.onPrimary, // 👈 números
                    selectorColor = MaterialTheme.colorScheme.primary,               // 👈 aguja
                    timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary, // 👈 caja hora seleccionada
                    timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.secondary,
                    timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            ) }
        )
    }
}

@Composable
fun TextFieldCustom(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    placeholder: String? = null,
    isPassword: Boolean = false,
    onFocusChanged: (Boolean) -> Unit = {}
) {

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    OutlinedTextField(
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
            focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,),
        value = value,
        onValueChange = onValueChange,

        label = {
            Text(label, color = MaterialTheme.colorScheme.onPrimaryContainer)
        },

        modifier = Modifier
            .fillMaxWidth(0.5f)
            .padding(bottom = 16.dp)
            .onFocusChanged { onFocusChanged(it.isFocused) },

        keyboardOptions =
            if (isPassword) {
                KeyboardOptions(
                    keyboardType = KeyboardType.Password
                )
            } else {
                keyboardOptions
            },

        placeholder = placeholder?.let {
            { Text(it, color = MaterialTheme.colorScheme.secondary) }
        },

        shape = RoundedCornerShape(32.dp),

        visualTransformation =
            if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },

        trailingIcon = {

            if (isPassword) {

                IconButton(
                    onClick = {
                        passwordVisible = !passwordVisible
                    }
                ) {

                    Icon(
                        imageVector =
                            if (passwordVisible) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                        contentDescription =
                            if (passwordVisible) {
                                "Ocultar contraseña"
                            } else {
                                "Mostrar contraseña"
                            }
                    )
                }
            }
        }
    )
}

sealed class DropdownSelection<out K> {
    data class Single<K>(val id: K?) : DropdownSelection<K>()
    data class Multiple<K>(val ids: List<K>) : DropdownSelection<K>()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T, K> DropdownCustom(
    section: String,
    items: List<T>,
    selection: DropdownSelection<K>,
    onItemSelected: (K) -> Unit,
    itemId: (T) -> K,
    itemName: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    val displayText = when (selection) {
        is DropdownSelection.Single ->
            items.find { itemId(it) == selection.id }?.let { itemName(it) } ?: "Seleccionar..."
        is DropdownSelection.Multiple ->
            if (selection.ids.isEmpty()) "Seleccionar..."
            else items
                .filter { itemId(it) in selection.ids }
                .joinToString(", ") { itemName(it) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text(section, color = MaterialTheme.colorScheme.onPrimaryContainer) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(0.5f),            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        )

        ExposedDropdownMenu(
            containerColor = MaterialTheme.colorScheme.tertiary,
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                val id = itemId(item)
                val isSelected = when (selection) {
                    is DropdownSelection.Single -> id == selection.id
                    is DropdownSelection.Multiple -> id in selection.ids
                }
                DropdownMenuItem(
                    text = { Text(itemName(item), color = MaterialTheme.colorScheme.onPrimaryContainer) },
                    onClick = {
                        onItemSelected(id)
                        if (selection is DropdownSelection.Single) expanded = false
                    },
                    trailingIcon = {
                        if (isSelected) Icon(Icons.Default.Check, contentDescription = null)
                    }
                )
            }
        }
    }
}

@Composable
fun CustomButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        content()
    }
}