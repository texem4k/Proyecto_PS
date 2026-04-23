package software.ulpgc.code.application.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Ballot
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


data class SideBarItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen,
)

// --- Composable principal ---

@Composable
fun SideBar(
    onNavigate: (Screen) -> Unit,
    selectedScreen: Screen
) {
    val topItems = listOf(
        SideBarItem("", Icons.Default.CalendarToday, Screen.CALENDAR),
        SideBarItem("", Icons.Default.Ballot, Screen.TASKS),
        SideBarItem("", Icons.Default.BarChart, Screen.STATS),
    )

    val bottomItems = listOf(
        SideBarItem("", Icons.Default.Settings, Screen.SETTINGS)
    )

    Column(
        modifier = Modifier
            .width(100.dp)
            .fillMaxHeight()
            .background(Color(0xFF1E1E2E))
            .padding(vertical = 24.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val home = SideBarItem("", Icons.Default.Home, Screen.HOME)
        SideBarNavItem(home, selectedScreen==home.screen, onClick = {onNavigate(home.screen)})

        Spacer(modifier = Modifier.weight(0.1f))
        topItems.forEach { item ->
            SideBarNavItem(
                item = item,
                isSelected = selectedScreen == item.screen,
                onClick = { onNavigate(item.screen) }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Empuja el resto al fondo
        Spacer(modifier = Modifier.weight(1f))

        // Iconos inferiores
        bottomItems.forEach { item ->
            SideBarNavItem(
                item = item,
                isSelected = selectedScreen == item.screen,
                onClick = { onNavigate(item.screen) }
            )
        }
    }
}

@Composable
private fun SideBarNavItem(
    item: SideBarItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor      = if (isSelected) Color(0xFF313244) else Color.Transparent
    val contentColor = if (isSelected) Color(0xFF89B4FA) else Color(0xFFCDD6F4)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = item.icon, contentDescription = item.label,
            tint = contentColor, modifier = Modifier.size(48.dp))
        Text(text = item.label, color = contentColor, fontSize = 14.sp)
    }

}