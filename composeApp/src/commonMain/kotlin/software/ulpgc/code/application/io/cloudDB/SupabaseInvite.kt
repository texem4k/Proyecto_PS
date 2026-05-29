package software.ulpgc.code.application.io.cloudDB

import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.Privilege
import kotlin.uuid.Uuid

object SupabaseInvite {

    suspend fun getCodes(group: Uuid): Map<Privilege, Long> {
        return SupabaseDBManager.getInviteCodes(group).getOrThrow().associate { Privilege.entries[it.privilege] to it.code }
    }

    suspend fun generateCode(group: Uuid, privilege: Privilege): Long {
        val code = Uuid.random().toULongs { _, leastSignificantBits -> leastSignificantBits }.toString().substring(0..9).toLong()
        SupabaseDBManager.setInviteCode(group, privilege, code).getOrThrow()
        return code
    }

    suspend fun removeCode(group: Uuid, privilege: Privilege) {
        SupabaseDBManager.removeCode(group, privilege)
    }

    suspend fun submitCode(code: Long): Boolean {
        if (SupabaseDBManager.codeExists(code)) {
            SupabaseDBManager.useCode(code, Store.currentUser())
            return true
        }
        return false
    }
}