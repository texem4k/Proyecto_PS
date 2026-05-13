package software.ulpgc.code.application.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Estados posibles de los diálogos
enum class DialogState {
    LOGIN,
    REGISTER,
    NONE
}

@Composable
fun AuthFlow(
    onDismiss: () -> Unit,
) {
    var dialogState by remember { mutableStateOf(DialogState.LOGIN) }

    LaunchedEffect(dialogState) {
        if (dialogState == DialogState.NONE) onDismiss()
    }
    when (dialogState) {
        DialogState.LOGIN -> LoginDialog(
            onDismiss    = { dialogState = DialogState.NONE },
            onCreateAccount = { dialogState = DialogState.REGISTER },
            onLoginSuccess  = { email, pass ->
                // TODO: lógica de login
                dialogState = DialogState.NONE
            }
        )
        DialogState.REGISTER -> RegisterDialog(
            onDismiss       = { dialogState = DialogState.NONE },
            onRegisterSuccess = { email, pass, name ->
                // TODO: lógica de registro
                dialogState = DialogState.NONE
            }
        )
        DialogState.NONE -> Unit
    }
}


@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onCreateAccount: () -> Unit,
    onLoginSuccess: (email: String, pass: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var pass  by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Inicia sesión") },
        text    = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextFieldCustom(
                    value         = email,
                    label         = "Email",
                    onValueChange = { email = it },
                    keyboardOptions = KeyboardOptions.Default,
                    isPassword    = false
                )
                TextFieldCustom(
                    value         = pass,
                    label         = "Contraseña",
                    onValueChange = { pass = it },
                    keyboardOptions = KeyboardOptions.Default,
                    isPassword    = true
                )
                Text("¿No tienes cuenta? Pulsa en Crear cuenta para registrarte.")
            }
        },
        confirmButton = {
            Button(onClick = { onLoginSuccess(email, pass) }) {
                Text("Iniciar sesión")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onCreateAccount) { Text("Crear cuenta") }
                Button(onClick = onDismiss)       { Text("Cancelar")     }
            }
        }
    )
}


@Composable
fun RegisterDialog(
    onDismiss: () -> Unit,
    onRegisterSuccess: (email: String, pass: String, name: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var name  by remember { mutableStateOf("") }
    var pass  by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Crear cuenta") },
        text    = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextFieldCustom(
                    value         = name,
                    label         = "Nombre de usuario",
                    onValueChange = { name = it },
                    keyboardOptions = KeyboardOptions.Default,
                    isPassword    = false
                )
                TextFieldCustom(
                    value         = email,
                    label         = "Correo electrónico",
                    onValueChange = { email = it },
                    keyboardOptions = KeyboardOptions.Default,
                    isPassword    = false
                )
                TextFieldCustom(
                    value         = pass,
                    label         = "Contraseña",
                    onValueChange = { pass = it },
                    keyboardOptions = KeyboardOptions.Default,
                    isPassword    = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onRegisterSuccess(email, pass, name) }) {
                Text("Crear cuenta")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

fun validateEmail(email: String): Boolean {
    return "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$".toRegex().matches(email)
}