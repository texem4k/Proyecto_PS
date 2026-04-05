package software.ulpgc.code.architecture.control

import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.TaskInterval
import software.ulpgc.code.architecture.model.times.TimeFactory
import kotlin.uuid.Uuid

class CommandBuilder (private val store: Storage) {
    private val args: MutableMap<String, String> = mutableMapOf()

    fun set(argName: String, argValue: String): CommandBuilder {
        args[argName] = argValue
        return this
    }

    fun build(type: CommandType): Command {
        return when(type) {
            CommandType.CREATE_TOPIC -> CreateTopicCommand(store, args["name"]!!, args["color"]!!.toInt())
            CommandType.CREATE_TAG -> CreateTagCommand(store, args["name"]!!, Uuid.parse(args["topicId"]!!))
            CommandType.CREATE_TASK -> CreateTaskCommand(store, args["priority"]!!.toInt(), args["name"]!!, Uuid.parse(args["userId"]!!),
                args["description"]!!, Uuid.parse(args["topicID"]!!), TimeFactory().parse(args["time"]!!), TaskInterval.valueOf(args["interval"]!!),
                args["tags"]!!.split(", ").map { Uuid.parse(it) }.toMutableList())
            CommandType.UPDATE_TOPIC -> TODO()
            CommandType.UPDATE_TAG -> TODO()
            CommandType.UPDATE_TASK -> TODO()
            CommandType.DELETE_TOPIC -> DeleteTopicCommand(store, Uuid.parse(args["id"]!!))
            CommandType.DELETE_TAG -> DeleteTagCommand(store, Uuid.parse(args["id"]!!))
            CommandType.DELETE_TASK -> DeleteTaskCommand(store, Uuid.parse(args["id"]!!))
        }
    }
}