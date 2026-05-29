package software.ulpgc.code.application.io.cloudDB

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.Group
import software.ulpgc.code.architecture.model.Privilege
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
        LogMaster.log("Se intenta hacer un inicio de sesion")
        auth.signInWith(Email) {
            email = userEmail
            password = userPassword
        }
        auth.startAutoRefreshForCurrentSession()
        Store.changeUserTo(Uuid.parse(auth.currentUserOrNull()?.id!!))
        LogMaster.log("Se ha completado del inicio de sesion")
    }

    suspend fun logout(): Result<Unit> = runCatching {
        LogMaster.log("Se intenta cerra sesion")
        auth.signOut()
        auth.stopAutoRefreshForCurrentSession()
        Store.onLogOut()
        LogMaster.log("Se ha completado el cierre de sesión")
    }

    suspend fun register(name: String, userEmail: String, userPassword: String): Result<Unit> = runCatching {
        LogMaster.log("Se intenta hacer un registro")
        val user = auth.signUpWith(Email) {
            email = userEmail
            password = userPassword
        }
        user?.id?.let { Store.add(User(name, Uuid.parse(it))) }
        login(userEmail, userPassword).getOrThrow()
        val gruop = Group("Grupo de $name", "Zona personal de trabajo",
            mutableMapOf(Uuid.parse(user!!.id) to Privilege.ADMIN),
            Uuid.parse(user.id), DBState.DISABLED)
        Store.add(gruop)
        LogMaster.log("Se ha completado el registro")
    }

    fun isLoggedIn(): Boolean {
        return auth.currentSessionOrNull() != null
    }
}