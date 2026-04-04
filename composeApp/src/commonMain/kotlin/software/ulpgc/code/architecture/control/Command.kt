package software.ulpgc.code.architecture.control

class Command (val execute: () -> Unit, val undo: ()-> Unit) {

}