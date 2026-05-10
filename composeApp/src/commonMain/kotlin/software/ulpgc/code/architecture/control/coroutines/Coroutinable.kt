package software.ulpgc.code.architecture.control.coroutines

interface Coroutinable {
    val delayMilis: Long
    suspend fun onInit()
    suspend fun execute()
    suspend fun onDispose()
}