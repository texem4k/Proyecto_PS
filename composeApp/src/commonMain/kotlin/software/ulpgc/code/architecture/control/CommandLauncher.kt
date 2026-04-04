package software.ulpgc.code.architecture.control

class CommandLauncher {
    private val commands: MutableList<Command> = mutableListOf()
    private var pivot: Int = 0

    fun launch(command: Command) {
        if (pivot < commands.size) {
            commands.removeAll(commands.subList(pivot, commands.size))
        }
        commands.add(command)
        command.execute()
        pivot++
    }

    fun undo() {
        if ( pivot < 1 ) { TODO("TIRAR ERROR") }
        commands[--pivot].undo()
    }

    fun redo() {
        if (pivot >= commands.size) { TODO("TIRAR ERROR") }
        commands[pivot++].execute()
    }
}
