package software.ulpgc.code.architecture.control.commands

enum class CommandType {
    CREATE_TOPIC, CREATE_TAG, CREATE_TASK,
    UPDATE_TOPIC, UPDATE_TAG ,UPDATE_TASK,
    DELETE_TOPIC, DELETE_TAG, DELETE_TASK,
    MARK_COMPLETE, UNMARK_COMPLETE
}