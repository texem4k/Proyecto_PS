package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.exceptions.CommandException
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.Topic
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.tasks.TaskInterval
import software.ulpgc.code.architecture.model.times.Time
import software.ulpgc.code.architecture.model.times.TimeFactory
import kotlin.uuid.Uuid

class CommandBuilder internal constructor (private val store: Storage) {
    private val args: MutableMap<String, String> = mutableMapOf()

    fun set(argName: String, argValue: String): CommandBuilder {
        args[argName] = argValue
        return this
    }

    fun build(type: CommandType): Result<Command> = runCatching {
        return Result.success(when(type) {
            CommandType.CREATE_TOPIC -> CreateTopicCommand(
                store,
                name().getOrThrow(),
                color().getOrThrow()
            )
            CommandType.CREATE_TAG -> CreateTagCommand(
                store,
                name().getOrThrow(),
                topicId().getOrThrow()
            )
            CommandType.CREATE_TASK -> CreateTaskCommand(
                store,
                priority().getOrThrow(),
                name().getOrThrow(),
                userId().getOrThrow(),
                description().getOrThrow(),
                topicId().getOrThrow(),
                time().getOrThrow(),
                interval().getOrThrow(),
                tags().getOrThrow()
            )
            CommandType.UPDATE_TOPIC -> UpdateTopicCommand(
                topic().getOrThrow(),
                name().getOrThrow(),
                color().getOrThrow()
            )
            CommandType.UPDATE_TAG -> UpdateTagCommand(
                tag().getOrThrow(),
                name().getOrThrow(),
                topicId().getOrThrow()
            )
            CommandType.UPDATE_TASK -> UpdateTaskCommand(
                task().getOrThrow(),
                priority().getOrThrow(),
                name().getOrThrow(),
                description().getOrThrow(),
                topicId().getOrThrow(),
                time().getOrThrow(),
                interval().getOrThrow(),
                tags().getOrThrow()
            )
            CommandType.DELETE_TOPIC -> DeleteTopicCommand(
                store,
                id().getOrThrow()
            )
            CommandType.DELETE_TAG -> DeleteTagCommand(
                store,
                id().getOrThrow()
            )
            CommandType.DELETE_TASK -> DeleteTaskCommand(
                store,id().getOrThrow()
            )
        })
    }

    private fun <T> getOrThrow(key: String, parse: (String) -> T): Result<T> = runCatching {
        return Result.success(parse(args[key] ?: throw CommandException("No existe argumento para $key")))
    }

    private fun tag(): Result<Tag> = runCatching {
        return Result.success(store.tags().find { id().getOrThrow() == it.id}
            ?: throw CommandException("No existe el tag ${id()} en el store"))
    }

    private fun task(): Result<Task> = runCatching {
        return Result.success(store.tasks().find { id().getOrThrow() == it.id}
            ?: throw CommandException("No existe el tarea ${id()} en el store"))
    }

    private fun topic(): Result<Topic> = runCatching {
        return Result.success(store.topics().find { id().getOrThrow() == it.id}
            ?: throw CommandException("No existe el topic ${id()} en el store"))
    }

    private fun interval(): Result<TaskInterval> = runCatching {
        return getOrThrow("interval", { interval -> TaskInterval.valueOf(interval)})
    }

    private fun time(): Result<Time> = runCatching {
        return getOrThrow("time", { time -> TimeFactory().parse(time) })
    }

    private fun id(): Result<Uuid> = runCatching {
        return getOrThrow("id", { id -> Uuid.parse(id)})
    }

    private fun topicId(): Result<Uuid> = runCatching {
        return getOrThrow("topicId", { id -> Uuid.parse(id)})
    }

    private fun userId(): Result<Uuid> = runCatching {
        return getOrThrow("userId", { id -> Uuid.parse(id)})
    }

    private fun name(): Result<String> = runCatching {
        return getOrThrow("name", { name -> name})
    }

    private fun description(): Result<String> = runCatching {
        return getOrThrow("description", { description -> description})
    }

    private fun tags(): Result<MutableSet<Uuid>> = runCatching {
        return getOrThrow("tags", { tags -> tags.split(", ").map { Uuid.parse(it) }.toMutableSet()})
    }

    private fun color(): Result<Int> = runCatching {
        return getOrThrow("color", { color -> color.toInt()})
    }

    private fun priority(): Result<Int> = runCatching {
        return getOrThrow("priority", { priority -> priority.toInt()})
    }
}