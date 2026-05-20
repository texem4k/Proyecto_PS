package software.ulpgc.code.application.io.cloudDB

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import org.jetbrains.compose.resources.ExperimentalResourceApi
import proyecto_ps.composeapp.generated.resources.Res
import software.ulpgc.code.architecture.control.coroutines.Coroutinable
import software.ulpgc.code.architecture.control.coroutines.CoroutineManager

object SupabaseProvider: Coroutinable {
    const val URL = "https://saedrkbiklymrcbnklwp.supabase.co"
    private lateinit var client: SupabaseClient
    lateinit var auth: Auth
        private set
    lateinit var postgrest: Postgrest
        private set

    fun initialize() {
        CoroutineManager.add(this)
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun getApiKey(): String {
        val bytes = Res.readBytes("files/composeResources/.env")
        return bytes.decodeToString()
    }

    override val delayMilis: Long = 1_000_000L

    override suspend fun onInit() {
        val apiKey = getApiKey()
        client = createSupabaseClient(URL, apiKey) {
            install(Postgrest)
            install(Auth)
        }
        auth = client.auth
        postgrest = client.postgrest
        SupabaseAuth.initialize(auth)
        SupabaseDBManager.initialize(postgrest)
    }

    override suspend fun execute() {
        return
    }

    override suspend fun onDispose() {
        client.close()
    }
}