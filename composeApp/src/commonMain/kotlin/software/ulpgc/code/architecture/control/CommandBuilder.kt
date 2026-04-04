package software.ulpgc.code.architecture.control

import software.ulpgc.code.architecture.io.Storage

class CommandBuilder (private val store: Storage) {
    private val args: MutableMap<String, String> = mutableMapOf()

    fun set(argName: String, argValue: String): CommandBuilder {
        args[argName] = argValue
        return this
    }

    fun build(type: CommandType): Command {
        return when(type) {
            CommandType.CREATE_TOPIC -> TODO()
            CommandType.CREATE_TAG -> TODO()
            CommandType.CREATE_TASK -> TODO()
            CommandType.UPDATE_TOPIC -> TODO()
            CommandType.UPDATE_TAG -> TODO()
            CommandType.UPDATE_TASK -> TODO()
            CommandType.DELETE_TOPIC -> TODO()
            CommandType.DELETE_TAG -> TODO()
            CommandType.DELETE_TASK -> TODO()
        }
    }
}