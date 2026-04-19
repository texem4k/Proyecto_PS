package software.ulpgc.code.architecture.model


enum class Priority(
    val text: String,
    val values: List<Int>,
) {
    VERYLOW("Muy baja (1-2)", listOf(1, 2)),
    LOW(" Baja (3-4)", listOf(3, 4)),
    MIDDLE("Media (5-6)", listOf(5, 6)),
    HIGH("Alta (7-8)", listOf(7, 8)),
    IMPORTANT("Urgente (9-10)", listOf(9, 10)),
}


