package software.ulpgc.code.application.ui

import androidx.compose.runtime.compositionLocalOf

public data class AuthState(
    val isAuthenticated: Boolean,
    val onLogin: () -> Unit,
    val onLogout: () -> Unit
)

val LocalAuthState = compositionLocalOf<AuthState> {
    error("No AuthState provided")
}