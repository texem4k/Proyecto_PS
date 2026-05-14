package software.ulpgc.code.application.ui

import Screen
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun UserMenuCard(
    modifier:Modifier,
    name: String,
    role: String,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = modifier
            .width(220.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {

            // Header: avatar + nombre
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
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(name, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 14.sp)
                    Text(role, color = Color.Gray, fontSize = 12.sp)
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE))

            // Opciones del menú
            MenuItemRow(Icons.Default.Settings, "Configuración", onClick = onDismiss)
            MenuItemRow(Icons.Default.HelpOutline, "Ayuda y soporte", hasArrow = true, onClick = onDismiss)

            HorizontalDivider(color = Color(0xFFEEEEEE))

            MenuItemRow(Icons.AutoMirrored.Filled.ExitToApp, "Cerrar sesión", onClick = onDismiss)
        }
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
        Text(label, fontSize = 14.sp, modifier = Modifier.weight(1f))
        if (hasArrow) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        }
    }
}