package software.ulpgc.code.architecture.control

import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.Topic
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.tasks.TaskInterval
import software.ulpgc.code.architecture.model.times.Time
import software.ulpgc.code.architecture.model.times.TimeFactory
import kotlin.time.Clock
import kotlin.uuid.Uuid

class CommandBuilder internal constructor (private val store: Storage) {
    private val args: MutableMap<String, String> = mutableMapOf()

    fun set(argName: String, argValue: String): CommandBuilder {
        args[argName] = argValue
        return this
    }

    fun build(type: CommandType): Command {
        return when(type) {
            CommandType.CREATE_TOPIC -> CreateTopicCommand(store, name(), color())
            CommandType.CREATE_TAG -> CreateTagCommand(store, name(), topicId())
            CommandType.CREATE_TASK -> CreateTaskCommand(store, priority(), name(), userId(), description(), topicId(), time(), interval(), tags())
            CommandType.UPDATE_TOPIC -> UpdateTopicCommand(topic(), name(), color())
            CommandType.UPDATE_TAG -> UpdateTagCommand(tag(), name(), topicId())
            CommandType.UPDATE_TASK -> UpdateTaskCommand(task(), priority(), name(), description(),topicId(),time(), interval(), tags())
            CommandType.DELETE_TOPIC -> DeleteTopicCommand(store, id())
            CommandType.DELETE_TAG -> DeleteTagCommand(store, id())
            CommandType.DELETE_TASK -> DeleteTaskCommand(store, id())
        }
    }

    //TODO errors
    private fun <T> getOrElse(key: String, parse: (String) -> T, default: T): T {
        if (args.containsKey(key)) {
            return parse(args[key]!!)
        }
        return default
    }

    private fun task(): Task {
        return store.tasks().find { id() == it.id}!!
    }

    private fun tag(): Tag {
        return store.tags().find { id() == it.id}!!
    }

    private fun topic(): Topic {
        return store.topics().find { id() == it.id}!!
    }

    private fun interval(): TaskInterval {
        return getOrElse("interval", {interval -> TaskInterval.valueOf(interval)}, TaskInterval.NONE)
    }

    private fun time(): Time {
        return getOrElse("time", { time -> TimeFactory().parse(time) }, TimeFactory().createTime(Clock.System.now(), 1))
    }

    private fun id(): Uuid {
        return getOrElse("id", {id -> Uuid.parse(id)}, Uuid.random())
    }

    private fun topicId(): Uuid {
        return getOrElse("topicId", {id -> Uuid.parse(id)}, Uuid.random())
    }

    private fun userId(): Uuid {
        return getOrElse("userId", {id -> Uuid.parse(id)}, Uuid.random())
    }

    private fun name(): String {
        return getOrElse("name", {name -> name}, "Nombre")
    }

    private fun description(): String {
        return getOrElse("description", {description -> description}, "")
    }

    private fun tags(): MutableList<Uuid> {
        return getOrElse("tags", {tags -> tags.split(", ").map { Uuid.parse(it) }.toMutableList()}, mutableListOf())
    }

    private fun color(): Int {
        return getOrElse("color", {color -> color.toInt()}, 1)
    }

    private fun priority(): Int {
        return getOrElse("priority", {priority -> priority.toInt()}, 1)
    }
}