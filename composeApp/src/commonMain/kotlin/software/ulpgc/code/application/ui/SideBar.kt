package software.ulpgc.code.application.ui

import Screen
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class SideBarItem(
    val icon: ImageVector,
    val screen: Screen,
)

private val topItems = listOf(
    SideBarItem(Icons.Default.CalendarToday, Screen.CALENDAR),
    SideBarItem(Icons.Default.Ballot, Screen.TASKS),
    SideBarItem(Icons.Default.BarChart, Screen.DASHBOARD),
    SideBarItem(Icons.Default.Palette, Screen.SETTINGS),
)

@Composable
fun SideBar(
    onNavigate: (Screen) -> Unit,
    selectedScreen: Screen,
    onSettingsClick: () -> Unit
) {
    var loginPushed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .width(100.dp)
            .fillMaxHeight()
            .background(Color(0xFF1E1E2E))
            .padding(vertical = 24.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val home = SideBarItem(Icons.Default.Home, Screen.HOME)

        SideBarNavItem(
            item = home,
            isSelected = selectedScreen == home.screen,
            onClick = { onNavigate(home.screen) }
        )

        HorizontalDivider(modifier=Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(32.dp))

        topItems.forEach { item ->
            if(item.screen == Screen.SETTINGS) {
                SideBarNavItem(
                    item = item,
                    isSelected = selectedScreen == item.screen,
                    onClick = onSettingsClick
                )
            } else{
                SideBarNavItem(
                    item = item,
                    isSelected = selectedScreen == item.screen,
                    onClick = { onNavigate(item.screen) }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.weight(1f))


        HorizontalDivider(modifier=Modifier.fillMaxWidth())

        SideBarNavItem(
            item = SideBarItem(Icons.Default.Person, Screen.PROFILE),
            isSelected = selectedScreen == Screen.PROFILE,
            onClick = {loginPushed=true}
        )

    }
    if(loginPushed) {
        key(loginPushed) {
            AuthFlow(
                onDismiss  = { loginPushed = false }
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
    val bgColor = if (isSelected) Color(0xFF313244) else Color.Transparent
    val contentColor = if (isSelected) Color(0xFF89B4FA) else Color(0xFFCDD6F4)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.screen.name,
            tint = contentColor,
            modifier = Modifier.size(48.dp)
        )
    }
}

fun AppThemeType.previewColor(): Color {
    return when (this) {
        AppThemeType.GREEN -> Color(0xFF3D6B4F)
        AppThemeType.GRAY -> Color(0xFF5E5CE6)
        AppThemeType.LAVANDA -> Color(0xFF7C5CBF)
        AppThemeType.BLUE -> Color(0xFF1A6B8A)
        AppThemeType.TERRACOTA -> Color(0xFF8B4A2E)
        AppThemeType.DARK -> Color(0xFF313244)
    }
}

@Composable
fun ThemeDialog(
    current: AppThemeType,
    onThemeSelected: (AppThemeType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecciona un tema") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                ThemeButton(AppThemeType.GREEN, "Verde", current, onThemeSelected)
                ThemeButton(AppThemeType.GRAY, "Gris", current, onThemeSelected)
                ThemeButton(AppThemeType.LAVANDA, "Lavanda", current, onThemeSelected)
                ThemeButton(AppThemeType.BLUE, "Azul", current, onThemeSelected)
                ThemeButton(AppThemeType.TERRACOTA, "Terracota", current, onThemeSelected)
                ThemeButton(AppThemeType.DARK, "Negro", current, onThemeSelected)

            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun ThemeButton(
    theme: AppThemeType,
    text: String,
    current: AppThemeType,
    onThemeSelected: (AppThemeType) -> Unit
) {
    val color = theme.previewColor()
    val selected = theme == current

    Button(
        onClick = { onThemeSelected(theme) },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) color else color.copy(alpha = 0.3f),
            contentColor = Color.White
        )
    ) {
        Text(text)
    }
}