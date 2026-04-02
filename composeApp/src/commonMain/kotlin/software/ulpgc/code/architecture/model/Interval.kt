package software.ulpgc.code.architecture.model

enum class Interval(val hours: Double) {
    Diario(24.0), Semanal(168.0), Mensual(720.0), Anual(8760.0)
}