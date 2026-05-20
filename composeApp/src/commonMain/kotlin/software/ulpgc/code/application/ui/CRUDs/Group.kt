package software.ulpgc.code.application.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import software.ulpgc.code.architecture.control.commands.CommandBuilder
import software.ulpgc.code.architecture.control.commands.CommandLauncher
import software.ulpgc.code.architecture.control.commands.CommandType
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.Group
import software.ulpgc.code.architecture.model.Privilege
import kotlin.collections.component1
import kotlin.collections.component2



data class MemberInvite(
    val email: String,
    val privilege: Privilege = Privilege.READER
)

data class CreateGroupFormState(
    val groupName: String           = "",
    val groupDescription: String    = "",
    val members: List<MemberInvite> = emptyList()
)
val CREATE_GROUP_WIZARD_STEPS = listOf(
    WizardStep("Info"),
    WizardStep("Miembros")
)

@Composable
fun CreateGroup(
    onClose: () -> Unit,
    onSubmit: (CreateGroupFormState) -> Unit = {}
) {
    var form      by remember { mutableStateOf(CreateGroupFormState()) }
    var step      by remember { mutableStateOf(0) }
    var formError by remember { mutableStateOf(false) }
    var errorMsg  by remember { mutableStateOf("") }

    if (formError) {
        AlertDialog(
            onDismissRequest = { formError = false },
            title   = { Text("Error") },
            text    = { Text(errorMsg) },
            confirmButton = {
                Button(onClick = { formError = false }) { Text("Aceptar") }
            }
        )
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier       = Modifier.fillMaxWidth(0.55f).wrapContentHeight(),
            shape          = RoundedCornerShape(20.dp),
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                WizardHeader(
                    title   = "Crear grupo",
                    step    = step,
                    steps   = CREATE_GROUP_WIZARD_STEPS,
                    onClose = onClose
                )

                Spacer(Modifier.height(20.dp))

                AnimatedContent(targetState = step, label = "group_wizard_step") { current ->
                    when (current) {
                        0 -> GroupStepBasicInfo(form = form, onFormChange = { form = it })
                        1 -> GroupStepMembers(form = form, onFormChange = { form = it })
                    }
                }

                Spacer(Modifier.height(24.dp))

                WizardNavigation(
                    step        = step,
                    totalSteps  = CREATE_GROUP_WIZARD_STEPS.size,
                    submitLabel = "Crear grupo",
                    onBack      = { step-- },
                    onNext      = {
                        val error = validateGroupStep0(form)
                        if (error != null) {
                            errorMsg = error; formError = true
                        } else {
                            val command = CommandBuilder()
                                .set("name", form.groupName)
                                .set("description", form.groupDescription)
                                .build(CommandType.CREATE_GROUP)
                            command
                                .onSuccess { CommandLauncher.launch(it)}
                                .onFailure { println("error: ${it.message}") }
                            step++
                        }
                    },
                    onSubmit = {
                        val error = validateGroupStep0(form)
                        if (error != null) {
                            errorMsg = error
                            formError = true
                            return@WizardNavigation
                        }
                        onSubmit(form)
                        onClose()
                    }
                )
            }
        }
    }
}



@Composable
fun GroupStepBasicInfo(
    form: CreateGroupFormState,
    onFormChange: (CreateGroupFormState) -> Unit
) {
    Column(
        modifier            = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StepLabel("Información del grupo")

        TextFieldCustom(
            value           = form.groupName,
            label           = "* Nombre del grupo",
            onValueChange   = { onFormChange(form.copy(groupName = it)) },
            keyboardOptions = KeyboardOptions.Default
        )

        TextFieldCustom(
            value           = form.groupDescription,
            label           = "Descripción",
            onValueChange   = { onFormChange(form.copy(groupDescription = it)) },
            keyboardOptions = KeyboardOptions.Default
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupStepMembers(
    form: CreateGroupFormState,
    onFormChange: (CreateGroupFormState) -> Unit
) {
    var selectedPriv by remember { mutableStateOf<Privilege?>(Privilege.READER) }
    var generatedCode by remember { mutableStateOf<String?>(null) }
    var generatedCodes by remember { mutableStateOf<Map<Privilege, String>>(emptyMap()) }
    val clipboard = LocalClipboardManager.current

    fun generateCode(): String {
        val chars = ('A'..'Z') + ('0'..'9')
        return (1..10).map { chars.random() }.joinToString("")
    }

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StepLabel("Invitar miembros")

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize(0.50f), horizontalArrangement = Arrangement.End) {
                DropdownCustom(
                    section = "Permisos",
                    items = Privilege.entries,
                    selection = DropdownSelection.Single(selectedPriv),
                    onItemSelected = { selectedPriv = it },
                    itemId = { it },
                    itemName = { it.name }
                )
            }

            OutlinedButton(
                onClick = {
                    selectedPriv?.let { priv ->
                        generatedCodes = generatedCodes + (priv to generateCode())
                    }
                },
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline
                ),
                contentPadding = PaddingValues(
                    horizontal = 20.dp,
                    vertical = 16.dp
                )
            ) {
                Icon(
                    Icons.Default.Link,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(6.dp))

                Text("Generar código")
            }

            ShowCode(generatedCode)
        }
        if (generatedCodes.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                generatedCodes.forEach { (priv, code) ->
                    CodeRow(
                        code = code,
                        privilege = priv,
                        onReset = {
                            generatedCodes = generatedCodes + (priv to generateCode())
                        },
                        clipboard
                    )
                }
            }
        }
    }
}

@Composable
fun MemberRow(
    member: MemberInvite,
    onPrivilegeChange: (Privilege) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = member.email,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        Box(modifier = Modifier.width(350.dp)) {
            DropdownCustom(
                section = "",
                items = Privilege.entries,
                selection = DropdownSelection.Single(member.privilege),
                onItemSelected = { onPrivilegeChange(it) },
                itemId = { it },
                itemName = { it.name }
            )
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Eliminar",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}


enum class EditGroupSection { INFO, MEMBERS, INVITE,SETTINGS }

@Composable
fun EditGroup(
    onDismiss: () -> Unit,
    onSubmit: (CreateGroupFormState) -> Unit = {},
    group: Group = Store.groups().first(),
) {
    var form by remember {
        mutableStateOf(
            CreateGroupFormState(
                groupName = group.name,
                groupDescription = group.description,
                members = group.users.map { (userId, privilege) ->
                    MemberInvite(
                        email = Store.users().find { it.id == userId }?.name ?: "",
                        privilege = privilege
                    )
                }
            )
        )
    }

    var section by remember { mutableStateOf(EditGroupSection.INFO) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.6f).wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gestionar grupo",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        modifier = Modifier
                            .weight(2f)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EditGroupSection.entries.forEach { s ->
                            val selected = section == s
                            val icon = when (s) {
                                EditGroupSection.INFO     -> Icons.Default.Info
                                EditGroupSection.MEMBERS  -> Icons.Default.Group
                                EditGroupSection.INVITE   -> Icons.Default.Link
                                EditGroupSection.SETTINGS -> Icons.Default.Settings
                            }
                            val label = when (s) {
                                EditGroupSection.INFO     -> "Información"
                                EditGroupSection.MEMBERS  -> "Miembros"
                                EditGroupSection.INVITE   -> "Invitar"
                                EditGroupSection.SETTINGS -> "Ajustes"
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primaryContainer
                                        else Color.Transparent
                                    )
                                    .clickable { section = s }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = label,
                                    fontSize = 9.sp,
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        IconButton(onClick = onDismiss) {
                            Text("✖\uFE0E", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }


                AnimatedContent(targetState = section, label = "edit_group_section") { current ->
                    when (current) {
                        EditGroupSection.INFO        -> GroupStepBasicInfo(form, { form = it })
                        EditGroupSection.MEMBERS     -> EditGroupSectionMembers(form, { form = it })
                        EditGroupSection.INVITE      ->  GroupStepMembers(form,{ form = it })
                        EditGroupSection.SETTINGS    -> EditGroupSectionSettings(
                            onSave = { onSubmit(form); onDismiss() },
                            onLeave = onDismiss
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditGroupSectionMembers(
    form: CreateGroupFormState,
    onFormChange: (CreateGroupFormState) -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StepLabel("Miembros del grupo")

        if (form.members.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                form.members.forEach { member ->
                    MemberRow(
                        member = member,
                        onPrivilegeChange = { newPriv ->
                            onFormChange(form.copy(
                                members = form.members.map {
                                    if (it.email == member.email) it.copy(privilege = newPriv) else it
                                }
                            ))
                        },
                        onRemove = {
                            onFormChange(form.copy(
                                members = form.members.filter { it.email != member.email }
                            ))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EditGroupSectionSettings(
    onSave: () -> Unit,
    onLeave: () -> Unit
) {
    var exit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StepLabel("Ajustes del grupo")

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Guardar configuración")
        }

        OutlinedButton(
            onClick = {exit=true},
            modifier = Modifier.fillMaxWidth(0.6f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Salir del grupo")
        }

        if(exit){
            ExitDialog(onChange={exit=false})
        }
    }
}

@Composable
fun ExitDialog(onChange: () -> Unit) {
    Dialog(onDismissRequest = onChange, content={
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Salir del grupo", modifier = Modifier.padding(bottom=16.dp,top=8.dp))


            Text("¿Estas seguro de que desea salir del grupo?", modifier = Modifier.padding(bottom=16.dp,top=8.dp))

            Row(){
                Button(onClick = {onChange()}) {
                    Text("Cancelar")
                }
                Button(onClick = {onChange()}, colors = ButtonDefaults.filledTonalButtonColors()) {
                    Text("Confirmar")
                }
            }


        }

    })
}

@Composable
fun CodeRow(
    code: String,
    privilege: Privilege,
    onReset: () -> Unit,
    clipboard: ClipboardManager
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = privilege.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp)
        )

        Text(
            text = code,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 2.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick = { clipboard.setText(AnnotatedString(code)) },
            modifier = Modifier.size(22.dp)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar",
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.primary)
        }

    }
}



@Composable
fun ShowCode(code: String?){
    code?.let { code ->
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = code,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
        }
    }
}


@Composable
fun JoinGroup(onDismiss: () -> Unit, onJoin: (String) -> Unit) {

    var code: String by remember { mutableStateOf("") }

    val isValid = code.length == 10
    val hasError = code.isNotEmpty() && code.length < 10
    val clipboard = LocalClipboardManager.current

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )

            Text(
                text = "Unirse a un grupo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Introduce el código",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            val containerColor = when {
                hasError -> MaterialTheme.colorScheme.errorContainer
                isValid  -> MaterialTheme.colorScheme.secondaryContainer
                else     -> MaterialTheme.colorScheme.primaryContainer
            }
            val borderColor = when {
                hasError -> MaterialTheme.colorScheme.error
                isValid  -> MaterialTheme.colorScheme.secondary
                else     -> MaterialTheme.colorScheme.primary
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(containerColor)
                    .border(1.dp, borderColor, RoundedCornerShape(24.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BasicTextField(
                    value = code,
                    onValueChange = { if (it.length <= 10) code = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (code.isEmpty()) {
                            Text(
                                text = "_ _ _ _ _ _ _ _ _ _",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        inner()
                    }
                )

                IconButton(
                    onClick = {
                        code = clipboard.getText()?.text?.trim()?.take(10)!!
                    },
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        Icons.Default.ContentPaste,
                        contentDescription = "Pegar código",
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (hasError) {
                    Text(
                        text = "El código debe tener 8 caracteres",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                Text(
                    text = "${code.length} / 10",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = { if (isValid) onJoin(code) },
                    enabled = isValid,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Unirse")
                }
            }
        }
    }
}