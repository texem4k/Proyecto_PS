package software.ulpgc.code.application.ui

import Screen
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Ballot
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import software.ulpgc.code.application.io.cloudDB.SupabaseAuth
import software.ulpgc.code.application.io.cloudDB.SupabaseAuth.isLoggedIn
import software.ulpgc.code.architecture.io.Store

@Composable
fun BottomBar(
    onNavigate: (Screen) -> Unit,
    selectedScreen: Screen,
    onSettingsClick: () -> Unit
) {
    var showPopup by remember { mutableStateOf(false) }
    var buttonBounds by remember { mutableStateOf(Rect(0f, 0f, 0f, 0f)) }
    var cardHeight by remember { mutableStateOf(0) }
    val authReady = SupabaseAuth.ready.collectAsState()


    val items = listOf(
        SideBarItem(Icons.Default.CalendarToday, Screen.CALENDAR),
        SideBarItem(Icons.Default.Ballot, Screen.TASKS),
        SideBarItem(Icons.Default.BarChart, Screen.DASHBOARD),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFF1E1E2E))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val home = SideBarItem(Icons.Default.Home, Screen.HOME)

        BottomBarNavItem(
            item = home,
            isSelected = selectedScreen == home.screen,
            onClick = { onNavigate(home.screen) }
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 12.dp)
                .width(1.dp)
                .background(Color(0xFF313244))
        )

        items.forEach { item ->
            BottomBarNavItem(
                item = item,
                isSelected = selectedScreen == item.screen,
                onClick = { onNavigate(item.screen) }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 12.dp)
                .width(1.dp)
                .background(Color(0xFF313244))
        )

        Box(modifier = Modifier.size(48.dp)) {
            if(authReady.value && isLoggedIn()) {
                GroupSelectorMenu(
                    groups = Store.groups().toList(),
                    selectedGroup = Store.currentGroup().id,
                    onGroupSelected = { },
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
            BottomBarNavItem(
                item = SideBarItem(Icons.Default.Person, Screen.PROFILE),
                isSelected = selectedScreen == Screen.PROFILE,
                onClick = { showPopup = true }
            )

            if (showPopup && authReady.value && isLoggedIn()) {
                Popup(
                    alignment = Alignment.BottomEnd,
                    offset = IntOffset(x = 0, y = -cardHeight),
                    onDismissRequest = { showPopup = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    UserMenuCard(
                        modifier = Modifier.onGloballyPositioned {
                            cardHeight = it.size.height
                        },
                        name = "Enrique Sosa",
                        role = "Invitado",
                        onDismiss = { showPopup = false }
                    )
                }
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
fun BottomBarNavItem(
    item: SideBarItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val background = if (isSelected) Color(0xFF313244) else Color.Transparent
    val iconTint = if (isSelected) Color(0xFFCDD6F4) else Color(0xFF6C7086)

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.screen.name,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
    }
}