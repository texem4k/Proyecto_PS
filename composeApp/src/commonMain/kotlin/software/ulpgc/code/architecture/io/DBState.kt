package software.ulpgc.code.architecture.io

import software.ulpgc.code.architecture.io.DBState.DELETED
import software.ulpgc.code.architecture.io.DBState.NEW
import software.ulpgc.code.architecture.io.DBState.UPDATED
import software.ulpgc.code.architecture.io.DBState.UNKNOWN

enum class DBState {
    DEFAULT, NEW, UPDATED, DELETED, UNKNOWN, CLEARED;
}

fun DBObject.isLocalNew()     = localDBState == NEW
fun DBObject.isLocalUpdated() = localDBState == UPDATED
fun DBObject.isLocalDeleted() = localDBState == DELETED
fun DBObject.isLocalUnknown() = localDBState == UNKNOWN
fun DBObject.isLocalCleared() = localDBState == DBState.CLEARED

fun DBObject.isCloudNew()     = localDBState == NEW
fun DBObject.isCloudUpdated() = localDBState == UPDATED
fun DBObject.isCloudDeleted() = localDBState == DELETED
fun DBObject.isCloudUnknown() = localDBState == UNKNOWN
fun DBObject.isCloudCleared() = localDBState == DBState.CLEARED