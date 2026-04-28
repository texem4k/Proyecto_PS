package software.ulpgc.code.architecture.control.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object CoroutineManager {
    private class Coroutine (val coroutinable: Coroutinable, val scope: CoroutineScope) {
        constructor (coroutinable: Coroutinable) : this(coroutinable, CoroutineScope(Dispatchers.Default+SupervisorJob()))

        fun run() {
            scope.launch {
                coroutinable.onInit()
                while (isActive) {
                    coroutinable.execute()
                    delay(coroutinable.delayMilis)
                }
            }
        }

        fun dispose() {
            scope.launch {
                coroutinable.onDispose()
            }.invokeOnCompletion {
                scope.cancel()
            }
        }

    }

    private val coroutines: MutableList<Coroutine> = mutableListOf()

    fun add(coroutinable: Coroutinable) {
        coroutines.add(Coroutine(coroutinable))
        coroutines.last().run()
    }

    fun dispose() {
        coroutines.reversed().forEach { coroutine -> coroutine.dispose() }
        coroutines.clear()
    }

}