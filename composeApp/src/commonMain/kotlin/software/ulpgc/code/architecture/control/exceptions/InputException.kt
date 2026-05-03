package software.ulpgc.code.architecture.control.exceptions

class InputException(msg: String) : AppException(msg) {

    override fun toString(): String {
        return "**InputException** [$time]: '$message'"
    }
}