package software.ulpgc.code.architecture.model

import software.ulpgc.code.architecture.io.DBState
import kotlin.uuid.Uuid

class EntityDeleter {
    fun delete(id: Uuid, entities: Sequence<Entity>) {
        entities.find { it.id == id }
            ?.dbState = DBState.DELETED
    }
}
