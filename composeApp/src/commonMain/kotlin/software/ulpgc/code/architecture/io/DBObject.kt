package software.ulpgc.code.architecture.io

interface DBObject {
    var localDBState: DBState
    var cloudDBState: DBState
}