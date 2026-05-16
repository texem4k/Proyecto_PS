package software.ulpgc.code.application.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class Privilege {
    MOD, CONTRIBUTOR, READER;

    val label: String
        get() = when (this) {
            MOD         -> "Moderador"
            CONTRIBUTOR -> "Colaborador"
            READER      -> "Lector"
        }
}

data class MemberInvite(
    val email: String,
    val privilege: Privilege = Privilege.READER
)

data class CreateGroupFormState(
    val groupName: String           = "",
    val groupDescription: String    = "",
    val members: List<MemberInvite> = emptyList()
)
private val GROUP_WIZARD_STEPS = listOf(
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
                    steps   = GROUP_WIZARD_STEPS,
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
                    totalSteps  = GROUP_WIZARD_STEPS.size,
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
private fun GroupStepBasicInfo(
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
@Composable
private fun GroupStepMembers(
    form: CreateGroupFormState,
    onFormChange: (CreateGroupFormState) -> Unit
) {
    var emailInput   by remember { mutableStateOf("") }
    var selectedPriv by remember { mutableStateOf<Privilege?>(Privilege.READER) }
    var emailError   by remember { mutableStateOf(false) }
    var errorMsg     by remember { mutableStateOf("") }

    fun addEmails() {
        val candidates = emailInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (candidates.isEmpty()) { emailError = true; errorMsg = "Introduce al menos un correo."; return }

        val invalid  = candidates.filter { !isValidEmail(it) }
        val dupes    = candidates.filter { e -> form.members.any { it.email == e } }
        val toAdd    = candidates.filter { isValidEmail(it) && form.members.none { m -> m.email == it } }

        when {
            invalid.isNotEmpty() -> {
                emailError = true
                errorMsg   = "Correo(s) inválido(s): ${invalid.joinToString(", ")}"
            }
            dupes.isNotEmpty() && toAdd.isEmpty() -> {
                emailError = true
                errorMsg   = "Ya están en la lista: ${dupes.joinToString(", ")}"
            }
            else -> {
                onFormChange(
                    form.copy(
                        members = form.members + toAdd.map {
                            MemberInvite(it, selectedPriv ?: Privilege.READER)
                        }
                    )
                )
                emailInput   = ""
                selectedPriv = Privilege.READER
                emailError   = false
            }
        }
    }

    Column(
        modifier            = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StepLabel("Invitar miembros")

        Row(
            modifier              = Modifier.fillMaxWidth(0.9f),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value           = emailInput,
                onValueChange   = { emailInput = it; emailError = false },
                label           = { Text("Correos (separados por comas)") },
                placeholder     = { Text("a@x.com, b@x.com") },
                isError         = emailError,
                singleLine      = true,
                modifier        = Modifier.weight(1f).padding(bottom = 16.dp),
                shape           = RoundedCornerShape(32.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Box(modifier = Modifier.width(350.dp)) {
                DropdownCustom(
                    section        = "Permisos",
                    items          = Privilege.entries,
                    selection      = DropdownSelection.Single(selectedPriv),
                    onItemSelected = { selectedPriv = it },
                    itemId         = { it },
                    itemName       = { it.label }
                )
            }

            FilledIconButton(
                onClick  = { addEmails() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }

        if (emailError) {
            Text(
                text  = errorMsg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (form.members.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                form.members.forEach { member ->
                    MemberRow(
                        member            = member,
                        onPrivilegeChange = { newPriv ->
                            onFormChange(
                                form.copy(
                                    members = form.members.map {
                                        if (it.email == member.email) it.copy(privilege = newPriv) else it
                                    }
                                )
                            )
                        },
                        onRemove = {
                            onFormChange(
                                form.copy(members = form.members.filter { it.email != member.email })
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MemberRow(
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
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text     = member.email,
            style    = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        Box(modifier = Modifier.width(350.dp)) {
            DropdownCustom(
                section        = "",
                items          = Privilege.entries,
                selection      = DropdownSelection.Single(member.privilege),
                onItemSelected = { onPrivilegeChange(it) },
                itemId         = { it },
                itemName       = { it.label }
            )
        }

        IconButton(
            onClick  = onRemove,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.Close,
                contentDescription = "Eliminar",
                tint               = MaterialTheme.colorScheme.error,
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}
