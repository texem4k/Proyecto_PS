package software.ulpgc.code.architecture.model

data class Task (
    val title: String,
    val topic: String,
    val dueDate: String,
    val priority: Int // 1 = más urgente
    )
