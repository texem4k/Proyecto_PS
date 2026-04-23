package software.ulpgc.code.architecture.control.commands

object CommandLauncher {
    private val commands: MutableList<List<Command>> = mutableListOf()
    private var pivot: Int = 0

    fun launch(command: Command) {
        if (pivot < commands.size) {
            commands.removeAll(commands.subList(pivot, commands.size))
        }
        commands.add(command.execute())
        pivot++
    }

    fun undo() {
        if ( pivot < 1 ) { return }
        pivot--
        commands[pivot] = commands[pivot].flatMap { command -> command.execute() }.subList(0, 1)
    }

    fun redo() {
        if (pivot >= commands.size) { return }
        commands[pivot] = commands[pivot].flatMap { command -> command.execute() }
        pivot++
    }
}
