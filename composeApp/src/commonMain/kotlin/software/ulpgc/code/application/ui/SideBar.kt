package software.ulpgc.code.application.ui

import Screen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import software.ulpgc.code.application.io.cloudDB.SupabaseAuth
import software.ulpgc.code.application.io.cloudDB.SupabaseAuth.isLoggedIn
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.Group
import kotlin.uuid.Uuid

data class SideBarItem(
    val icon: ImageVector,
    val screen: Screen,
)

val topItems = listOf(
    SideBarItem(Icons.Default.CalendarToday, Screen.CALENDAR),
    SideBarItem(Icons.Default.Ballot, Screen.TASKS),
    SideBarItem(Icons.Default.BarChart, Screen.DASHBOARD),
)

@Composable
fun SideBar(
    onNavigate: (Screen) -> Unit,
    selectedScreen: Screen,
    onSettingsClick: () -> Unit,
    version: Int,
    onRefresh: () -> Unit,
) {

    var showPopup by remember { mutableStateOf(false) }
    var buttonBounds by remember { mutableStateOf(Rect.Zero) }
    var cardHeight by remember { mutableStateOf(0) }
    val authReady = SupabaseAuth.ready.collectAsState()

    val currentUserId = Store.currentUser()

    val currentUserName =
        Store.users().firstOrNull { it.id == currentUserId }?.name ?: "Usuario"

    val group = Store.currentGroup()
    val isLocalGroup = group.name == "local"

    Column(
        modifier = Modifier
            .width(100.dp)
            .fillMaxHeight()
            .background(Color(0xFF1E1E2E))
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val home = SideBarItem(Icons.Default.Home, Screen.HOME)

        SideBarNavItem(
            item = home,
            isSelected = selectedScreen == home.screen,
            onClick = { onNavigate(home.screen) }
        )

        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(32.dp))

        topItems.forEach { item ->
            SideBarNavItem(
                item = item,
                isSelected = selectedScreen == item.screen,
                onClick = { onNavigate(item.screen) }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.weight(0.05f))

        key(version) {
            if (authReady.value && isLoggedIn()) {
                GroupSelectorMenu(
                    groups = Store.groups().toList(),
                    selectedGroup = Store.currentGroup().id,
                    onGroupSelected = {
                        Store.changeGroupTo(Store.groups().find { g -> g.id == it }!!)
                        onRefresh()
                    }
                )
            }
        }

        Box(
            modifier = Modifier.onGloballyPositioned { coordinates ->
                val pos = coordinates.positionInRoot()
                buttonBounds = Rect(
                    left = pos.x,
                    top = pos.y,
                    right = pos.x + coordinates.size.width,
                    bottom = pos.y + coordinates.size.height
                )
            }
        ) {
            SideBarNavItem(
                item = SideBarItem(Icons.Default.Person, Screen.PROFILE),
                isSelected = selectedScreen == Screen.PROFILE,
                onClick = { showPopup = true }
            )

            if (showPopup && authReady.value && isLoggedIn()) {

                val density = LocalDensity.current
                val offsetY = with(density) {
                    (-cardHeight + 250.dp.toPx()).toInt()
                }

                Popup(
                    alignment = Alignment.BottomStart,
                    offset = IntOffset(
                        x = buttonBounds.right.toInt() + 8,
                        y = offsetY
                    ),
                    onDismissRequest = { showPopup = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    UserMenuCard(
                        modifier = Modifier.onGloballyPositioned {
                            cardHeight = it.size.height
                        },
                        name = currentUserName,
                        role = "Invitado",
                        isLocalGroup = isLocalGroup,
                        onDismiss = { showPopup = false }
                    )
                }

                onRefresh()

            } else if (showPopup && authReady.value) {
                AuthFlow(
                    onDismiss = { showPopup = false },
                    onAuthSuccess = {
                        showPopup = false
                    }
                )
            }
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
        containerColor = MaterialTheme.colorScheme.onPrimary,
        onDismissRequest = onDismiss,
        title = { Text("Selecciona un tema", color = Color.Black) },
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


@Composable
fun GroupSelectorMenu(
    groups: List<Group>,
    selectedGroup: Uuid,
    onGroupSelected: (Uuid) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentGroup = groups.find { it.id == selectedGroup }

    Row(verticalAlignment = Alignment.CenterVertically) {

        Surface(
            modifier = Modifier
                .width(140.dp)
                .clickable { expanded = true },
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if ((currentGroup?.name?.length ?: 0) > 10)
                        "${currentGroup?.name?.take(10)}..."
                    else
                        currentGroup?.name ?: "Seleccionar grupo",
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFCDD6F4),
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }

        Box {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                offset = DpOffset(x = 20.dp, y = (-15).dp),
                modifier = Modifier.heightIn(max = (3 * 48).dp)
            ) {
                groups.forEach { group ->
                    val isSelected = group.id == selectedGroup
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = group.name,
                                fontSize = 14.sp,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        },
                        onClick = {
                            onGroupSelected(group.id)
                            expanded = false
                        },
                        trailingIcon = {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}