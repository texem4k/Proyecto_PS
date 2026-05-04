package software.ulpgc.code.application.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ModernoColorScheme = lightColorScheme(
    primary = Color(0xFF5E5CE6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE5E5EA),
    onPrimaryContainer = Color(0xFF1C1C1E),

    secondary = Color(0xFF8E8E93),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE5E5EA),
    onSecondaryContainer = Color(0xFF1C1C1E),

    tertiary = Color(0xFF5E5CE6),
    onTertiary = Color(0xFFFFFFFF),

    background = Color(0xFFF5F5F7),
    onBackground = Color(0xFF1C1C1E),

    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF8E8E93),

    inverseSurface = Color(0xFF2C2C2E),

    outline = Color(0xFF8E8E93),
    outlineVariant = Color(0xFFE5E5EA),

    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ModernoColorScheme,
        content = content
    )
}