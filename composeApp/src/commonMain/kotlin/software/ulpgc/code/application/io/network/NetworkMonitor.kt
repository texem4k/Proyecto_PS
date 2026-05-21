package software.ulpgc.code.application.io.network

import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.PollResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import software.ulpgc.code.application.io.cloudDB.SupabaseProvider

object NetworkMonitor {
    private val _state: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val hasConnection = _state.asStateFlow()

    private val connectivity = Connectivity {
        autoStart = true
        urls(SupabaseProvider.URL)
        pollingIntervalMs = 5_000
        onPollResult { response ->
            when (response) {
                is PollResult.Error -> _state.value = false
                is PollResult.Response -> _state.value = true
            }
        }
    }

}