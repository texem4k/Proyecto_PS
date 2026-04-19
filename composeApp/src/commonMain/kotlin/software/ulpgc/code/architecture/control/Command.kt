package software.ulpgc.code.architecture.control


interface Command {
    fun execute(): Command
}