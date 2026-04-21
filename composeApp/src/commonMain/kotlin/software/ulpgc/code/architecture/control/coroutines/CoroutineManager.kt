package software.ulpgc.code.architecture.control.coroutines

object CoroutineManager {
    val coroutines : MutableList<Coroutine> = mutableListOf()

    // TODO Hacer que cada corutina se le asigne un hilo de ejecución.
    fun add(coroutine: Coroutine) {
        coroutines.add(coroutine)
        coroutine.onInit()
    }

    fun dispose() {
        coroutines.reversed().forEach { coroutine -> coroutine.onDispose() }
    }

}