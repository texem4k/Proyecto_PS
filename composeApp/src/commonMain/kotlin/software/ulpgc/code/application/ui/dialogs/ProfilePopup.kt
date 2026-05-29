package software.ulpgc.code.application.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import software.ulpgc.code.application.io.cloudDB.SupabaseAuth.logout
import software.ulpgc.code.application.ui.EditGroup
import software.ulpgc.code.application.ui.JoinGroup
import software.ulpgc.code.application.ui.LocalThemeState
import software.ulpgc.code.architecture.control.coroutines.runBlocking
import software.ulpgc.code.architecture.io.Store
import kotlin.uuid.Uuid


@Composable
fun UserMenuCard(
    modifier: Modifier,
    name: String,
    role: String,
    onDismiss: () -> Unit,
) {
    val theme = LocalThemeState.current
    var manageGroups by remember { mutableStateOf(false) }
    var joinGroup by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .width(220.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {

            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFB0BEC5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.first().toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(role, color = Color.DarkGray, fontSize = 12.sp)
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE))

            if (Store.currentGroup().id != Uuid.parse("00000000-0000-0000-0000-000000000000")){
                MenuItemRow(
                    Icons.Default.Group,
                    "Gestión de grupos",
                    onClick = { manageGroups = true }
                )
            }

            MenuItemRow(
                Icons.Default.GroupWork,
                "Unirse a un grupo",
                onClick = { joinGroup = true }
            )

            MenuItemRow(
                Icons.Default.Settings,
                "Configuración",
                onClick = onDismiss
            )

            HorizontalDivider(color = Color(0xFFEEEEEE))

            MenuItemRow(
                Icons.AutoMirrored.Filled.ExitToApp,
                "Cerrar sesión",
                onClick = {
                    runBlocking { logout() }
                    onDismiss()
                }
            )
        }
    }

    if (manageGroups) {
        EditGroup({ manageGroups = false }, onSubmit = {})
    }

    if (joinGroup) {
        JoinGroup({ joinGroup = false }, { "" })
    }
}
@Composable
fun MenuItemRow(
    icon: ImageVector,
    label: String,
    hasArrow: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp), tint = Color.DarkGray)
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 14.sp, modifier = Modifier.weight(1f), color = Color.Black)
        if (hasArrow) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        }
    }
}