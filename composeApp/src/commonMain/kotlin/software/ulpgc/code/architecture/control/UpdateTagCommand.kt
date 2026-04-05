package software.ulpgc.code.architecture.control

import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.Tag
import kotlin.uuid.Uuid

class UpdateTagCommand (private val store: Storage,private val id: Uuid,private val tag: Tag): Command {

//    constructor(store: Storage, id: Uuid, name: String, topicId: Uuid) : this(
//        store, id , store.tags().find { id==it.id }!!.copy(name = name, topicId = topicId),
//    )

    override fun execute(): Command {
        val clonOriginTag = store.tags().find { id==it.id }!!.copy()
        store
        return UpdateTagCommand(clonOriginTag)
    }
}