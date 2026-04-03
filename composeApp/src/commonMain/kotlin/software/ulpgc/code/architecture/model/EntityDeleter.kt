package software.ulpgc.code.architecture.model

import software.ulpgc.code.architecture.io.DBState
import kotlin.uuid.Uuid

object EntityDeleter {
    fun delete(id: Uuid, entities: Sequence<Entity>) {
        entities.find { it.id == id }
            ?.dbState = DBState.DELETED
    }
}
