package software.ulpgc.code.architecture.control.commands


interface Command {
    fun execute(): List<Command>
}