package software.ulpgc.code.application.ui.dialogs

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import software.ulpgc.code.application.ui.dataStructure.TextFieldCustom

@Composable
fun EmailField(
    email: String,
    onEmailChange: (String) -> Unit,
    touched: Boolean,
    valid: Boolean,
    onFocusChanged: (Boolean) -> Unit
) {
    TextFieldCustom(
        value = email,
        label = "Email",
        onValueChange = onEmailChange,
        keyboardOptions = KeyboardOptions.Default,
        isPassword = false,
        onFocusChanged = onFocusChanged
    )
    if (touched && !valid) {
        Text(
            "El formato del email no es válido",
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun PasswordField(
    pass: String,
    onPassChange: (String) -> Unit,
    touched: Boolean,
    valid: Boolean,
    onFocusChanged: (Boolean) -> Unit
) {
    TextFieldCustom(
        value = pass,
        label = "Contraseña",
        onValueChange = onPassChange,
        keyboardOptions = KeyboardOptions.Default,
        isPassword = true,
        onFocusChanged = onFocusChanged
    )
    if (touched && !valid) {
        Text(
            "El formato de la contraseña no es válida\nDebe contener mínimo 8 carácteres, con un dígito y una mayúscula.",
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}