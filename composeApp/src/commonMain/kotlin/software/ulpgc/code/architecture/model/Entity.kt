package software.ulpgc.code.architecture.model

import software.ulpgc.code.architecture.io.DBState
import kotlin.uuid.Uuid

interface Entity {
    val id: Uuid?
    var dbState: DBState
}