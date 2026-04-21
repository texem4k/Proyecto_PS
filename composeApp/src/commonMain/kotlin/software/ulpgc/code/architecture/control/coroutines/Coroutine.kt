package software.ulpgc.code.architecture.control.coroutines

interface Coroutine {
    val delayMilis: Long
    suspend fun onInit()
    suspend fun execute()
    suspend fun onDispose()
}