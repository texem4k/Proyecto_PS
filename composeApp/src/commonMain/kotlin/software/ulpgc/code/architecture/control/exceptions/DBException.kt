package software.ulpgc.code.architecture.control.exceptions

class DBException(msg: String) : AppException(msg) {

    override fun toString(): String {
        return "**DBException** [$time]: '$message'"
    }
}