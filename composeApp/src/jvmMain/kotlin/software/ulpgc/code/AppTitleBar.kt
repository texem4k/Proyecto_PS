package software.ulpgc.code

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.launch
import software.ulpgc.code.application.io.cloudDB.SupabaseAuth
import software.ulpgc.code.application.io.cloudDB.SupabaseAuth.logout
import software.ulpgc.code.architecture.control.coroutines.runBlocking

val SidebarColor = Color(0xFF1E1E2E)

@Composable
fun WindowScope.AppTitleBar(
    windowState: WindowState,
    onClose: () -> Unit,
    sidebarWidth: Int = 100
) {
    WindowDraggableArea {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {

            Box(
                modifier = Modifier
                    .width(sidebarWidth.dp)
                    .fillMaxHeight()
                    .background(SidebarColor)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primaryContainer),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Minimizar
                IconButton(
                    onClick = {
                        windowState.isMinimized = true
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Minimizar",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Maximizar / Restaurar
                IconButton(
                    onClick = {
                        windowState.placement =
                            if (windowState.placement == WindowPlacement.Maximized) {
                                WindowPlacement.Floating
                            } else {
                                WindowPlacement.Maximized
                            }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CropSquare,
                        contentDescription = "Maximizar",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(16.dp)
                    )
                }
                val scope = rememberCoroutineScope()
                IconButton(
                    onClick = {
                        scope.launch {
                            logout().getOrThrow()
                            onClose()
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color(0xFFCF6679),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}