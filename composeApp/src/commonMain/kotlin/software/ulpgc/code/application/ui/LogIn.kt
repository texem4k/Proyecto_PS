package software.ulpgc.code.application.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class DialogState {
    LOGIN,
    REGISTER,
    NONE
}

@Composable
fun AuthFlow(
    onDismiss: () -> Unit,
    onAuthSuccess: () -> Unit,
) {
    var dialogState by remember { mutableStateOf(DialogState.LOGIN) }

    LaunchedEffect(dialogState) {
        if (dialogState == DialogState.NONE) onDismiss()
    }
    when (dialogState) {
        DialogState.LOGIN -> LoginDialog(
            onDismiss    = { dialogState = DialogState.NONE },
            onCreateAccount = { dialogState = DialogState.REGISTER },
            onLoginSuccess  = { _, _ ->
                onAuthSuccess()
                dialogState = DialogState.NONE
            }
        )
        DialogState.REGISTER -> RegisterDialog(
            onDismiss       = { dialogState = DialogState.NONE },
            onRegisterSuccess = { _, _, _ ->
                onAuthSuccess()
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
    var validEmail by remember { mutableStateOf(false) }
    var validPass by remember { mutableStateOf(false) }
    var touch by remember { mutableStateOf(false) }

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

                if(!validEmail&&touch){
                    Text("El formato del email no es válido", color = Color.Red)
                }
                TextFieldCustom(
                    value         = pass,
                    label         = "Contraseña",
                    onValueChange = { pass = it },
                    keyboardOptions = KeyboardOptions.Default,
                    isPassword    = true
                )
                if(!validPass&&touch){
                    Text("El formato de la contraseña no es válida\nDebe contener mínimo 8 carácteres, con un dígito y una mayúscula.", color = Color.Red, textAlign = TextAlign.Center)
                }
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
                        touch = true
                        if (validateEmail(email)) validEmail = true
                        if (validatePassword(pass)) validPass = true
                        if (validEmail && validPass) onLoginSuccess(email, pass)
                    }
                ) { Text("Iniciar sesión") }

                CustomButton(onClick = onCreateAccount) { Text("Crear cuenta") }

                CustomButton(onClick = onDismiss) { Text("Cancelar") }
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
    var validEmail by remember { mutableStateOf(false) }
    var validPass by remember { mutableStateOf(false) }
    var validUser by remember { mutableStateOf(false) }
    var touch by remember { mutableStateOf(false) }
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
            Button(onClick = {
                touch=true
                if(validateEmail(email)) {
                    validEmail = true
                }
                if(validatePassword(pass)) {
                    validPass = true
                }

                if(validateUsername(name)) {
                    validUser = true
                }

                if(validEmail&& validPass&&validUser) {
                    onRegisterSuccess(email, pass, name) }
                }
            ){
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

fun validatePassword(pass: String): Boolean {
    return "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}\$".toRegex().matches(pass)
}

fun validateUsername(user: String): Boolean {
    return !user.isEmpty()
}
