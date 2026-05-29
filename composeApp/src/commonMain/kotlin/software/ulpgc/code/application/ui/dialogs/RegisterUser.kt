package software.ulpgc.code.application.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import software.ulpgc.code.application.io.cloudDB.SupabaseAuth.register
import software.ulpgc.code.application.ui.dataStructure.TextFieldCustom
import software.ulpgc.code.application.ui.validators.validateEmail
import software.ulpgc.code.application.ui.validators.validatePassword
import software.ulpgc.code.architecture.control.coroutines.runBlocking

@Composable
fun RegisterDialog(
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var name  by remember { mutableStateOf("") }
    var pass  by remember { mutableStateOf("") }
    var validEmail by remember { mutableStateOf(false) }
    var validPass by remember { mutableStateOf(false) }
    var validUser by remember { mutableStateOf(false) }

    var nameFocused by remember { mutableStateOf(false) }
    var nameTouched by remember { mutableStateOf(false) }
    var emailFocused by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(false) }
    var passFocused  by remember { mutableStateOf(false) }
    var passTouched  by remember { mutableStateOf(false) }

    var errRegister by remember { mutableStateOf(false) }

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
                    value = name,
                    label = "Nombre de usuario",
                    onValueChange = { name = it },
                    keyboardOptions = KeyboardOptions.Default,
                    isPassword = false,
                    onFocusChanged = { focused ->
                        if (nameFocused && !focused) nameTouched = true
                        nameFocused = focused
                        validUser = name.isNotEmpty()
                    }
                )
                if (nameTouched && !validUser) {
                    Text(
                        "El nombre no puede estar vacío.",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                EmailField(
                    email = email,
                    onEmailChange = { email = it },
                    touched = emailTouched,
                    valid = validEmail,
                    onFocusChanged = { focused ->
                        if (emailFocused && !focused) emailTouched = true
                        emailFocused = focused
                        validEmail = validateEmail(email)
                    }
                )

                PasswordField(
                    pass = pass,
                    onPassChange = { pass = it },
                    touched = passTouched,
                    valid = validPass,
                    onFocusChanged = { focused ->
                        if (passFocused && !focused) passTouched = true
                        passFocused = focused
                        validPass = validatePassword(pass)
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (validEmail && validPass && validUser) {
                    try {
                        runBlocking { register(name, email, pass) }.getOrThrow()
                        onDismiss()
                    } catch (_: Exception) {
                        errRegister = true
                    }
                }
            }) {
                Text("Crear cuenta")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    if (errRegister) {
        AlertDialog(
            onDismissRequest = { errRegister = false },
            text = {
                Text("Datos inválidos, comprueba de nuevo o crea una cuenta nueva")
            },
            confirmButton = {
                Button(onClick = { errRegister = false }) {
                    Text("Confirmar")
                }
            }
        )
    }
}