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
fun DBObject.isLocalDeleted() = localDBState == DELETED || localDBState == DBState.CLEARED
fun DBObject.isLocalUnknown() = localDBState == UNKNOWN
fun DBObject.isLocalCleared() = localDBState == DBState.CLEARED

fun DBObject.isCloudNew()     = cloudDBState == NEW
fun DBObject.isCloudUpdated() = cloudDBState == UPDATED
fun DBObject.isCloudDeleted() = cloudDBState == DELETED || cloudDBState == DBState.CLEARED
fun DBObject.isCloudUnknown() = cloudDBState == UNKNOWN
fun DBObject.isCloudCleared() = cloudDBState == DBState.CLEARED