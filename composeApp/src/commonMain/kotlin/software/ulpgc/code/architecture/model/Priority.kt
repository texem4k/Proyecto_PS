package software.ulpgc.code.architecture.model


enum class Priority(val value: Int, val text: String) {
    P1(1, "Muy baja"),
    P2(2, "Muy baja"),
    P3(3, "Baja"),
    P4(4, "Baja"),
    P5(5, "Media"),
    P6(6, "Media"),
    P7(7, "Alta"),
    P8(8, "Alta"),
    P9(9, "Urgente"),
    P10(10, "Urgente");

    companion object {
        fun fromValue(value: Int) = entries.first { it.value == value }
    }
}