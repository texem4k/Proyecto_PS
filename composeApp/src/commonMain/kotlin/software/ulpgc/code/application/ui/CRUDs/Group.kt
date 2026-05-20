package software.ulpgc.code.application.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
                        if (error != null) { errorMsg = error; formError = true }
                        else step++
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

    fun generateCode(): String {
        val chars = ('A'..'Z') + ('0'..'9')
        return (1..8).map { chars.random() }.joinToString("")
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
                        }
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
    onClose: () -> Unit,
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
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.6f).wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gestionar grupo",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onClose) {
                        Text("✖\uFE0E", color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Segmented control
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                ) {
                    EditGroupSection.entries.forEach { s ->
                        val selected = section == s
                        val label = when (s) {
                            EditGroupSection.INFO         -> "Información"
                            EditGroupSection.MEMBERS      -> "Miembros"
                            EditGroupSection.INVITE       -> "Invitar"
                            EditGroupSection.SETTINGS     -> "Ajustes"
                        }
                        val icon = when (s) {
                            EditGroupSection.INFO         -> Icons.Default.Info
                            EditGroupSection.MEMBERS      -> Icons.Default.Group
                            EditGroupSection.INVITE      -> Icons.Default.Link
                            EditGroupSection.SETTINGS     -> Icons.Default.Settings
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .clickable { section = s }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(22.dp),
                                tint = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Contenido de la sección
                AnimatedContent(targetState = section, label = "edit_group_section") { current ->
                    when (current) {
                        EditGroupSection.INFO        -> GroupStepBasicInfo(form, { form = it })
                        EditGroupSection.MEMBERS     -> EditGroupSectionMembers(form, { form = it })
                        EditGroupSection.INVITE     -> EditGroupSectionInvitations(form, { form = it })
                        EditGroupSection.SETTINGS    -> EditGroupSectionSettings(
                            onSave = { onSubmit(form); onClose() },
                            onLeave = onClose
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditGroupSectionInvitations(
    form: CreateGroupFormState,
    onFormChange: (CreateGroupFormState) -> Unit
){
    var selectedPriv by remember { mutableStateOf<Privilege?>(Privilege.READER) }
    var generatedCode by remember { mutableStateOf<String?>(null) }

    fun generateCode(): String {
        val chars = ('A'..'Z') + ('0'..'9')
        return (1..8).map { chars.random() }.joinToString("")
    }

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StepLabel("Miembros del grupo")

        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
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
                onClick = { generatedCode = generateCode() },
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Generar código")
            }

            ShowCode(generatedCode)
        }
    }
}


// ── Sección 3: Miembros ───────────────────────────────────────────────────────

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

        // Lista de miembros actuales
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

// ── Sección 4: Ajustes ────────────────────────────────────────────────────────

@Composable
private fun EditGroupSectionSettings(
    onSave: () -> Unit,
    onLeave: () -> Unit
) {
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
            onClick = onLeave,
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
    }
}

@Composable
fun CodeRow(
    code: String,
    privilege: Privilege,
    onReset: () -> Unit
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
        // Privilegio
        Text(
            text = privilege.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp)
        )

        // Código generado
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
            onClick = { /* reset */ },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Resetear código",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
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
            IconButton(
                onClick = { /* clipboard */ },
                modifier = Modifier.size(22.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar",
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}