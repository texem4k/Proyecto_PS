package software.ulpgc.code.architecture.io

import software.ulpgc.code.architecture.io.DBState.DELETED
import software.ulpgc.code.architecture.io.DBState.NEW
import software.ulpgc.code.architecture.io.DBState.UPDATED

enum class DBState {
    DEFAULT, NEW, UPDATED, DELETED;
}

fun DBObject.isNew()     = dbState == NEW
fun DBObject.isUpdated() = dbState == UPDATED
fun DBObject.isDeleted() = dbState == DELETED