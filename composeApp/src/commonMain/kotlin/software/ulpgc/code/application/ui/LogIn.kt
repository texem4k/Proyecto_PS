package software.ulpgc.code.application.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import software.ulpgc.code.application.io.cloudDB.SupabaseAuth
import software.ulpgc.code.application.io.cloudDB.SupabaseAuth.login
import software.ulpgc.code.application.io.cloudDB.SupabaseAuth.register
import software.ulpgc.code.architecture.control.coroutines.runBlocking
import software.ulpgc.code.architecture.control.logs.LogMaster

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
            onDismiss       = { dialogState = DialogState.NONE }
        )
        DialogState.NONE -> Unit
    }
}


@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onCreateAccount: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
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
                TextFieldCustom(
                    value         = email,
                    label         = "Email",
                    onValueChange = { email = it },
                    keyboardOptions = KeyboardOptions.Default,
                    isPassword    = false,
                    onFocusChanged  = { focused ->
                        if (emailFocused && !focused) emailTouched = true
                        emailFocused = focused
                        validEmail = validateEmail(email)
                    }
                )

                if (emailTouched && !validEmail){
                    Text(
                        "El formato del email no es válido",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                TextFieldCustom(
                    value         = pass,
                    label         = "Contraseña",
                    onValueChange = { pass = it },
                    keyboardOptions = KeyboardOptions.Default,
                    isPassword    = true,
                    onFocusChanged  = { focused ->
                        if (passFocused && !focused) passTouched = true
                        passFocused = focused
                        validPass = validatePassword(pass)
                    }
                )

                if(!validPass && passTouched){
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
                        if (validEmail && validPass){
                            try{
                                runBlocking {login(email, pass)}
                                onDismiss()
                            } catch (e: Exception){
                                errLogin=true
                            }
                        }
                    }
                ) { Text("Iniciar sesión") }

                CustomButton(onClick = onCreateAccount) { Text("Crear cuenta") }

                CustomButton(onClick = onDismiss) { Text("Cancelar") }
            }
        }
    )

    if(errLogin){
        AlertDialog(
            onDismissRequest = {errLogin=false},
            text = {
                Text("Datos inválidos, comprueba de nuevo o crea una cuenta nueva")
            },
            confirmButton = {
                Button(onClick = {errLogin=true}){
                    Text("Confirmar")
                }
            }
        )
    }
}


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
    var touch by remember { mutableStateOf(false) }

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
                    value         = name,
                    label         = "Nombre de usuario",
                    onValueChange = { name = it },
                    keyboardOptions = KeyboardOptions.Default,
                    isPassword    = false,
                    onFocusChanged  = { focused ->
                        if (nameFocused && !focused) nameTouched = true
                        nameFocused = focused
                        validUser = !name.isEmpty()
                    }
                )
                if (nameTouched && !validUser){
                    Text("El nombre no puede estar vacío.",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                TextFieldCustom(
                    value         = email,
                    label         = "Correo electrónico",
                    onValueChange = { email = it },
                    keyboardOptions = KeyboardOptions.Default,
                    isPassword    = false,
                    onFocusChanged  = { focused ->
                        if (emailFocused && !focused) emailTouched = true
                        emailFocused = focused
                        validEmail = validateEmail(email)
                    }
                )
                if (emailTouched && !validEmail){
                    Text(
                        "El formato del email no es válido",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                TextFieldCustom(
                    value         = pass,
                    label         = "Contraseña",
                    onValueChange = { pass = it },
                    keyboardOptions = KeyboardOptions.Default,
                    isPassword    = true,
                    onFocusChanged  = { focused ->
                        if (passFocused && !focused) passTouched = true
                        passFocused = focused
                        validEmail = validatePassword(pass)
                    }
                )
                if (passTouched && !validPass){
                    Text("El formato de la contraseña no es válida\nDebe contener mínimo 8 carácteres, con un dígito y una mayúscula.", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if(validEmail&& validPass&&validUser) {
                    try{
                        runBlocking{register(name, email, pass)}.getOrThrow()
                        onDismiss()
                    }catch(e: Exception){
                        LogMaster.log(e.toString())
                        LogMaster.log(e.message!!)
                        errRegister = true
                    }
                }
            }){
                Text("Crear cuenta")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancelar") }
        }
    )
    if(errRegister){
        AlertDialog(
            onDismissRequest = {errRegister=false},
            text = {
                Text("Datos inválidos, comprueba de nuevo o crea una cuenta nueva")
            },
            confirmButton = {
                Button(onClick = {errRegister=false}){
                    Text("Confirmar")
                }
            }
        )
    }
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
