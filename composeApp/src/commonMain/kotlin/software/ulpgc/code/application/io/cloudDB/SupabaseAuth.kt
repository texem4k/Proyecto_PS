package software.ulpgc.code.application.io.cloudDB

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import software.ulpgc.code.architecture.control.commands.CommandBuilder
import software.ulpgc.code.architecture.control.commands.CommandType
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.User
import kotlin.uuid.Uuid

object SupabaseAuth {
    private lateinit var auth: Auth
    private val _ready = MutableStateFlow(false)
    val ready = _ready.asStateFlow()

    fun initialize(auth: Auth) {
        this.auth = auth
        _ready.value = true
    }

    suspend fun login(userEmail: String, userPassword: String): Result<Unit> = runCatching {
        auth.signInWith(Email) {
            email = userEmail
            password = userPassword
        }
        auth.startAutoRefreshForCurrentSession()
        Store.changeUserTo(Uuid.parse(auth.currentUserOrNull()?.id!!))
    }

    suspend fun logout(): Result<Unit> = runCatching {
        auth.signOut()
        auth.stopAutoRefreshForCurrentSession()
        Store.onLogOut()
    }

    suspend fun register(name: String, userEmail: String, userPassword: String): Result<Unit> = runCatching {
        val user = auth.signUpWith(Email) {
            email = userEmail
            password = userPassword
        }
        user?.id?.let { Store.add(User(name, Uuid.parse(it))) }
        login(userEmail, userPassword)
        CommandBuilder().set("name", "Grupo de $name")
            .set("description", "Zona personal de trabajo")
            .build(CommandType.CREATE_GROUP)
            .getOrThrow()
            .execute()
    }

    fun isLoggedIn(): Boolean {
        return auth.currentSessionOrNull() != null
    }

    suspend fun refresh() {
        auth.refreshCurrentSession()
    }
}