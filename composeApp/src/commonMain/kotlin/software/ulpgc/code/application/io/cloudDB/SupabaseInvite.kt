package software.ulpgc.code.application.io.cloudDB

import software.ulpgc.code.architecture.model.Privilege
import kotlin.uuid.Uuid

object SupabaseInvite {
    val ready = SupabaseDBManager.ready

    suspend fun getCodes(group: Uuid): Map<Privilege, Int> {
        return SupabaseDBManager.getInviteCodes(group).getOrThrow().associate { Privilege.entries[it.privilege] to it.code }
    }

    suspend fun generateCode(group: Uuid, privilege: Privilege): Int {
        val code = Uuid.random().toString().split("-").joinToString("").substring(0..7).toInt()
        SupabaseDBManager.setInviteCode(group, privilege, code)
        return code
    }

    suspend fun removeCode(group: Uuid, privilege: Privilege) {
        SupabaseDBManager.removeCode(group, privilege)
    }
}