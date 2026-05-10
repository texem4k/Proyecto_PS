package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.exceptions.CommandException
import software.ulpgc.code.architecture.control.logs.LogMaster

object CommandLauncher {
    private val commands: MutableList<List<Command>> = mutableListOf()
    private var pivot: Int = 0

    fun launch(command: Command) {
        if (pivot < commands.size) {
            LogMaster.log("Limpiando cache de comandos")
            commands.removeAll(commands.subList(pivot, commands.size))
        }
        LogMaster.log("Ejecutando comandos:")
        commands.add(command.execute())
        pivot++
    }

    fun canUndo(): Boolean {
        return pivot >= 1
    }

    fun undo(): Result<Unit> = runCatching {
        if ( !canUndo() ) throw CommandException("No hay comandos para deshacer")
        pivot--
        LogMaster.log("Deshaciendo comandos:")
        commands[pivot] = commands[pivot].flatMap { command -> command.execute() }.subList(0, 1)
    }

    fun canRedo(): Boolean {
        return pivot < commands.size
    }

    fun redo(): Result<Unit> = runCatching {
        if (!canRedo()) { throw CommandException("No hay comandos para rehacer") }
        LogMaster.log("Rehaciendo comandos:")
        commands[pivot] = commands[pivot].flatMap { command -> command.execute() }
        pivot++
    }
}
