package software.ulpgc.code.architecture.control.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import software.ulpgc.code.architecture.control.logs.LogMaster

object CoroutineManager {
    private class Coroutine (val coroutinable: Coroutinable, val scope: CoroutineScope) {
        constructor (coroutinable: Coroutinable) : this(coroutinable, CoroutineScope(Dispatchers.Default+SupervisorJob()))

        fun run() {
            scope.launch {
                LogMaster.log("Iniciando corutina $coroutinable")
                coroutinable.onInit()
                while (isActive) {
                    LogMaster.log("Ejecutando corutina $coroutinable")
                    coroutinable.execute()
                    delay(coroutinable.delayMilis)
                }
            }
        }

        suspend fun dispose() {
            LogMaster.log("Desechando corutina $coroutinable")
            val job = scope.launch {
                coroutinable.onDispose()
            }
            job.invokeOnCompletion {
                scope.cancel()
            }
            job.join()
        }

    }

    private val coroutines: MutableList<Coroutine> = mutableListOf()

    fun add(coroutinable: Coroutinable) {
        coroutines.add(Coroutine(coroutinable))
        coroutines.last().run()
    }

    fun dispose(onFinish: () -> Unit) {
        LogMaster.log("Desechando corutinas activas")
        val disposeScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        disposeScope.launch {
            coroutines.reversed().forEach { coroutine ->
                coroutine.dispose()
            }
        }.invokeOnCompletion {
            LogMaster.log("Corutinas desechadas")
            coroutines.clear()
            onFinish()
        }
    }

}