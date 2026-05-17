package software.ulpgc.code.application.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import kotlinx.datetime.TimeZone
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.Group
import software.ulpgc.code.architecture.model.Privilege
import software.ulpgc.code.architecture.model.Tag
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.uuid.Uuid


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

val EDIT_GROUP_WIZARD_STEPS = listOf(
    WizardStep("Información"),
    WizardStep("Tópicos y Tags"),
    WizardStep("Miembros"),
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
fun EditGroup(
    onClose: () -> Unit,
    onSubmit: (CreateGroupFormState) -> Unit = {},
    group:Group=Store.groups().first(),
) {
    var form      by remember { mutableStateOf(CreateGroupFormState()) }
    form = form.copy(groupName = group.name, groupDescription = group.description)

    group.users.forEach { (user,privilege) ->
        form = form.copy(members = form.members.plus(MemberInvite(Store.users().find{ u -> u.id==user}?.name!!, privilege)))
    }
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
                    title   = "Gestionar grupo",
                    step    = step,
                    steps   = EDIT_GROUP_WIZARD_STEPS,
                    onClose = onClose
                )

                Spacer(Modifier.height(20.dp))

                AnimatedContent(targetState = step, label = "group_wizard_step") { current ->
                    when (current) {
                        0 -> GroupStepSelectGroup(form = form, onFormChange = { form = it })
                        1 -> GroupStepTopicTags(form = form, onFormChange = { form = it })
                        2 -> GroupStepMembers(form = form, onFormChange = { form = it })
                    }
                }

                Spacer(Modifier.height(24.dp))

                WizardNavigation(
                    step        = step,
                    totalSteps  = EDIT_GROUP_WIZARD_STEPS.size,
                    submitLabel = "Editar grupo",
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
fun GroupStepSelectGroup(form: CreateGroupFormState,
                         onFormChange: (CreateGroupFormState) -> Unit) {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StepLabel("Selección de grupo")
        var actualGroup by remember { mutableStateOf("Tu tablero en la nube") }
        var expand by remember { mutableStateOf(false) }

        DropdownMenu(
            expanded = expand,
            onDismissRequest = { expand = false },
            modifier = Modifier.fillMaxWidth(0.15f)
        ) {

            for(g in gro){
                DropdownMenuItem(
                    text = { Text(g) },
                    onClick = {
                        actualGroup = g
                        expand = false
                    }
                )
            }
        }
        //Si el usuario es lector...
        Text("Mensaje para cuando el usuario no tiene privilegios en el grupo elegido")
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

@Composable
fun GroupStepTopicTags(
    form: CreateGroupFormState,
    onFormChange: (CreateGroupFormState) -> Unit
) {
    val topics = Store.topics().take(2).toList()
    val tags = remember(topics) {
        topics.flatMap { topic ->
            Store.tags().filter { tag -> tag.topicId == topic.id }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StepLabel("Tópicos y tags")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Columna izquierda: Tópicos ──
            Card(modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tópicos",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(topics) { t ->
                            Card(
                                modifier = Modifier.fillMaxWidth(0.95f),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = t.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Columna derecha: Tags ──
            Card(modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(tags) { tag ->
                            Card(
                                modifier = Modifier.fillMaxWidth(0.95f),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = tag.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun GroupStepMembers(
    form: CreateGroupFormState,
    onFormChange: (CreateGroupFormState) -> Unit
) {
    var emailInput by remember { mutableStateOf("") }
    var selectedPriv by remember { mutableStateOf<Privilege?>(Privilege.READER) }
    var emailError by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    fun addEmails() {
        val candidates = emailInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (candidates.isEmpty()) {
            emailError = true; errorMsg = "Introduce al menos un correo."; return
        }

        val invalid = candidates.filter { !isValidEmail(it) }
        val dupes = candidates.filter { e -> form.members.any { it.email == e } }
        val toAdd = candidates.filter { isValidEmail(it) && form.members.none { m -> m.email == it } }

        when {
            invalid.isNotEmpty() -> {
                emailError = true
                errorMsg = "Correo(s) inválido(s): ${invalid.joinToString(", ")}"
            }

            dupes.isNotEmpty() && toAdd.isEmpty() -> {
                emailError = true
                errorMsg = "Ya están en la lista: ${dupes.joinToString(", ")}"
            }

            else -> {
                onFormChange(
                    form.copy(
                        members = form.members + toAdd.map {
                            MemberInvite(it, selectedPriv ?: Privilege.READER)
                        }
                    )
                )
                emailInput = ""
                selectedPriv = Privilege.READER
                emailError = false
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StepLabel("Invitar miembros")

        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it; emailError = false },
                label = { Text("Correos (separados por comas)") },
                placeholder = { Text("a@x.com, b@x.com") },
                isError = emailError,
                singleLine = true,
                modifier = Modifier.weight(1f).padding(bottom = 16.dp),
                shape = RoundedCornerShape(32.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Box(modifier = Modifier.width(350.dp)) {
                DropdownCustom(
                    section = "Permisos",
                    items = Privilege.entries,
                    selection = DropdownSelection.Single(selectedPriv),
                    onItemSelected = { selectedPriv = it },
                    itemId = { it },
                    itemName = { it.name }
                )
            }

            FilledIconButton(
                onClick = { addEmails() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }

        if (emailError) {
            Text(
                text = errorMsg,
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
                        member = member,
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

