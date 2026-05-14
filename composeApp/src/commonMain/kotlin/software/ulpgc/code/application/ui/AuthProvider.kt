package software.ulpgc.code.application.ui

import androidx.compose.runtime.compositionLocalOf

// AuthProvider.kt
val LocalAuthState = compositionLocalOf<AuthState> { AuthState.Unauthenticated }
val LocalOnAuthSuccess = compositionLocalOf<(UserSession) -> Unit> { {} }
val LocalOnLogout = compositionLocalOf<() -> Unit> { {} }