package software.ulpgc.code.application.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import software.ulpgc.code.application.io.cloudDB.SupabaseAuth
import software.ulpgc.code.application.io.cloudDB.SupabaseAuth.login
import software.ulpgc.code.application.ui.dataStructure.CustomButton
import software.ulpgc.code.application.ui.validators.validateEmail
import software.ulpgc.code.application.ui.validators.validatePassword
import software.ulpgc.code.architecture.control.coroutines.runBlocking

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
    val authReady = SupabaseAuth.ready.collectAsState()


    LaunchedEffect(dialogState) {
        if (dialogState == DialogState.NONE) onDismiss()
    }

    if(!authReady.value){
        return
    }
    when (dialogState) {
        DialogState.LOGIN -> LoginDialog(
            onDismiss    = { dialogState = DialogState.NONE },
            onCreateAccount = { dialogState = DialogState.REGISTER },
        )
        DialogState.REGISTER -> RegisterDialog(
            onDismiss = { dialogState = DialogState.NONE }
        )
        DialogState.NONE -> Unit
    }
}


@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onCreateAccount: () -> Unit,
) {
    var loginEmail by remember { mutableStateOf("") }
    var pass  by remember { mutableStateOf("") }
    var validEmail by remember { mutableStateOf(false) }
    var validPass by remember { mutableStateOf(false) }
    var errLogin by remember { mutableStateOf(false) }

    var emailFocused by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(false) }
    var passFocused  by remember { mutableStateOf(false) }
    var passTouched  by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Inicia sesión") },
        text    = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EmailField(
                    email = loginEmail,
                    onEmailChange = { loginEmail = it },
                    touched = emailTouched,
                    valid = validEmail,
                    onFocusChanged = { focused ->
                        if (emailFocused && !focused) emailTouched = true
                        emailFocused = focused
                        validEmail = validateEmail(loginEmail)
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

                Spacer(Modifier.height(20.dp))
                Text("¿No tienes cuenta? Pulsa en Crear cuenta para registrarte.", textAlign = TextAlign.Center)
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CustomButton(
                    onClick = {
                        if (validEmail && validPass) {
                            try {
                                runBlocking { login(loginEmail, pass) }
                                onDismiss()
                            } catch (_: Exception) {
                                errLogin = true
                            }
                        }
                    }
                ) { Text("Iniciar sesión") }

                CustomButton(onClick = onCreateAccount) { Text("Crear cuenta") }

                CustomButton(onClick = onDismiss) { Text("Cancelar") }
            }
        }
    )

    if (errLogin) {
        AlertDialog(
            onDismissRequest = { errLogin = false },
            text = {
                Text("Datos inválidos, comprueba de nuevo o crea una cuenta nueva")
            },
            confirmButton = {
                Button(onClick = { errLogin = false }) {
                    Text("Confirmar")
                }
            }
        )
    }
}


