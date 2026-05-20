package software.ulpgc.code.architecture.control.coroutines

import kotlinx.coroutines.runBlocking as coroutinesRunBlocking

actual fun <T> runBlocking(block: suspend () -> T): T =
    coroutinesRunBlocking { block() }