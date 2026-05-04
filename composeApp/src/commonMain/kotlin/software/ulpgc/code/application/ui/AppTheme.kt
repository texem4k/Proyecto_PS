package software.ulpgc.code.application.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeType {
    GREEN,
    GRAY,
    LAVANDA,
    BLUE,
    TERRACOTA
}

private val GreenColorScheme = lightColorScheme(
    primary = Color(0xFF3D6B4F),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB8D4C0),
    onPrimaryContainer = Color(0xFF1A3326),
    background = Color(0xFFEFF4F0),
    onBackground = Color(0xFF1A1C1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1A),
    surfaceVariant = Color(0xFFD4E4D8),
    onSurfaceVariant = Color(0xFF4A6352),
    inverseSurface = Color(0xFF2D3B30),
    inverseOnSurface = Color(0xFFEFF4F0),
    outline = Color(0xFF6B8C73),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
)

private val GrayColorScheme = lightColorScheme(
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

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1C1E),

    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF8E8E93),

    outline = Color(0xFF8E8E93),
    outlineVariant = Color(0xFFE5E5EA),

    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
)

private val LavandaColorScheme = lightColorScheme(
    primary = Color(0xFF7C5CBF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD4CAE8),
    onPrimaryContainer = Color(0xFF2D2640),

    secondary = Color(0xFF6B6580),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD4CAE8),
    onSecondaryContainer = Color(0xFF2D2640),

    tertiary = Color(0xFF7C5CBF),
    onTertiary = Color(0xFFFFFFFF),

    background = Color(0xFFEDE8F5),
    onBackground = Color(0xFF1C1B1F),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1F),

    surfaceVariant = Color(0xFFD4CAE8),
    onSurfaceVariant = Color(0xFF6B6580),

    outline = Color(0xFF6B6580),
    outlineVariant = Color(0xFFD4CAE8),

    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
)

private val BlueColorScheme = lightColorScheme(
    primary = Color(0xFF1A6B8A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB8D8E8),
    onPrimaryContainer = Color(0xFF0A2F3D),
    background = Color(0xFFEDF4F8),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFCCE4EF),
    onSurfaceVariant = Color(0xFF3D6478),
    inverseSurface = Color(0xFF1A2D38),
    inverseOnSurface = Color(0xFFEDF4F8),
    outline = Color(0xFF4A8AA8),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
)

private val TerracotaColorScheme = lightColorScheme(
    primary = Color(0xFF8B4A2E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8C4B0),
    onPrimaryContainer = Color(0xFF3D1A0A),
    background = Color(0xFFF8F0EC),
    onBackground = Color(0xFF1C1A18),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1A18),
    surfaceVariant = Color(0xFFEDD8CC),
    onSurfaceVariant = Color(0xFF6B4030),
    inverseSurface = Color(0xFF3D2518),
    inverseOnSurface = Color(0xFFF8F0EC),
    outline = Color(0xFF9E6650),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
)

@Composable
fun AppTheme(
    theme: AppThemeType,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AppThemeType.GREEN -> GreenColorScheme
        AppThemeType.GRAY -> GrayColorScheme
        AppThemeType.LAVANDA -> LavandaColorScheme
        AppThemeType.BLUE -> BlueColorScheme
        AppThemeType.TERRACOTA -> TerracotaColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}