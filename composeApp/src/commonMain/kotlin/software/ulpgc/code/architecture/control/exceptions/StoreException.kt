package software.ulpgc.code.architecture.control.exceptions

class StoreException(msg: String) : AppException(msg) {

    override fun toString(): String {
        return "**StoreException** [$time]: '$message'"
    }
}