package software.ulpgc.code.application.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.Privilege
import kotlin.uuid.Uuid



data class GroupState(
    var name: String = "",
    var description: String = "",
    var id: Uuid? = null,
    var users: Map<Uuid, Privilege>? = null
)



val groups = Store.groups()


@Composable
fun ManageGroups(onDismiss: () -> Unit) {
    var groupState by remember {mutableStateOf(GroupState())}

    Dialog(
        onDismissRequest = {onDismiss()},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(modifier = Modifier
            .padding(8.dp),
            shape = RoundedCornerShape(10.dp)
        ){
            Column(
                modifier = Modifier.fillMaxWidth(0.75f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Gestión de grupos",
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                DropdownCustom(
                    section = "Grupos",
                    items = groups.toList(),
                    selection = DropdownSelection.Single(groupState.id),
                    onItemSelected = { id -> groupState = groupState.copy(id = id) },
                    itemId = { it.id },
                    itemName = { it.name }
                )

                if (groupState.id != null) {

                    Row(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                        TextFieldCustom(
                            value = groupState.name,
                            label = "Nombre del grupo",
                            onValueChange = { groupState = groupState.copy(name = it) },
                            keyboardOptions = KeyboardOptions.Default
                        )
                    }

                    Row(modifier = Modifier.padding(8.dp).fillMaxWidth(0.6f), horizontalArrangement = Arrangement.Start) {
                        TextFieldCustom(
                            value = groupState.name,
                            label = "Descripción del grupo",
                            onValueChange = { groupState = groupState.copy(name = it) },
                            keyboardOptions = KeyboardOptions.Default
                        )
                    }
                        TextFieldCustom(
                            value = groupState.description,
                            label = "Descripción del grupo",
                            onValueChange = { groupState = groupState.copy(description = it) },
                            keyboardOptions = KeyboardOptions.Default
                        )

                }

                /*
                    val idUser = "00000000-0000-0000-0000-000000000000"
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxWidth(0.5f),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalArrangement = Arrangement.spacedBy(64.dp)
                    ) {


                    val g = Store.groups().filter { group -> group.users.contains(idUser) }.filter { x -> x.users.values.contains(
                        Privilege.ADMIN) }
                    items(Store.groups().toList()) { group ->
                        Card(){
                            Text(text = group.name)
                        }
                    }
                    }
                     */
                Row() {
                    if (groupState.id != null) {
                        Button(onClick = { onDismiss() }) {
                            Text("Salir del grupo")
                        }
                    }

                    Button(onClick = { onDismiss() }) {
                        Text("Cancelar")
                    }
                }


            }
        }
    }
}