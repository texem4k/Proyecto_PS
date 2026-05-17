package software.ulpgc.code.application.io.cloudDB

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import software.ulpgc.code.architecture.io.DBManager
import software.ulpgc.code.architecture.io.DBObject
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.Group
import software.ulpgc.code.architecture.model.Priority
import software.ulpgc.code.architecture.model.Privilege
import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.Topic
import software.ulpgc.code.architecture.model.User
import software.ulpgc.code.architecture.model.tasks.CompletionStat
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.tasks.TaskInterval
import software.ulpgc.code.architecture.model.times.TimeFactory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class UserData(val id: Uuid, val name: String) {
    constructor(user: User) : this(user.id, user.name)

    fun parse(): User {
        return User(name, id, DBState.UNKNOWN, DBState.DEFAULT)
    }
}

@Serializable
data class TopicData(val id: Uuid, val groupId: Uuid, val name: String, val color: Int){
    constructor(topic: Topic) : this(topic.id, topic.groupId, topic.name, topic.color.toArgb())

    fun parse(): Topic {
        return Topic(name, Color(color), groupId, id, DBState.UNKNOWN, DBState.DEFAULT)
    }
}

@Serializable
data class TagData(val id: Uuid, val topicId: Uuid, val name: String,){
    constructor(tag: Tag) : this(tag.id,tag.topicId,tag.name)

    fun parse(): Tag {
        return Tag(name, topicId, id, DBState.UNKNOWN, DBState.DEFAULT)
    }
}

@Serializable
data class GroupUserData(val userId: Uuid, val groupId: Uuid, val privilege: Privilege){
    companion object {
        fun serializeFrom(group: Group): List<GroupUserData> {
            return group.users.entries.map { GroupUserData(it.key, group.id, it.value) }
        }

        fun parse(users: List<GroupUserData>): MutableMap<Uuid, Privilege> {
            return users.associate { it.userId to it.privilege }.toMutableMap()
        }
    }
}

@Serializable
data class GroupData(val id: Uuid, val name: String, val description: String){
    constructor(group: Group) : this(group.id,group.name, group.description)

    fun parse(users: MutableMap<Uuid, Privilege>): Group {
        return Group(name, description, users, id, DBState.UNKNOWN, DBState.DEFAULT)
    }
}

@Serializable
data class TaskTagData(val taskId: Uuid, val tagId: Uuid, val topicId: Uuid){
    companion object {
        fun serializeFrom(task: Task): List<TaskTagData> {
            return task.tags.map { TaskTagData(task.id, it, task.topicId) }
        }

        fun parse(data: List<TaskTagData>):  MutableSet<Uuid> {
            return data.map { it.tagId }.toMutableSet()
        }
    }
}

@Serializable
data class AssignuserTaskData(val taskId: Uuid, val userId: Uuid, val groupId: Uuid){
    companion object{
        fun serializeFrom(task: Task): List<AssignuserTaskData>{
            return task.users.map { AssignuserTaskData(
                task.id, it , Store.topics().first { topic -> topic == task.topicId }.groupId ) }
        }

        fun parse(data: List<AssignuserTaskData>): MutableSet<Uuid>{
            return data.map { it.userId }.toMutableSet()
        }
    }
}

@Serializable
data class InvitationsData(val groupId: Uuid, val code: Int, val privilege: Int)

@Serializable
data class TaskData(val id: Uuid, val topicId: Uuid, val groupId: Uuid,
    val name: String, val description: String, val time: String,
    val interval: Int, val priority: Int, val isCompleted: Boolean
){
    constructor(task: Task) : this(task.id,task.topicId, Store.topics().first { it == task.topicId }.groupId,
        task.name, task.description, task.time.toString(), task.interval.ordinal, task.priority.ordinal,
        task.isCompleted)

    fun parse(tags: MutableSet<Uuid>, users: MutableSet<Uuid>): Task {
        return Task(topicId, name, description, TimeFactory.parse(time), TaskInterval.entries[interval],
            Priority.entries[priority], tags, users, isCompleted, id, DBState.UNKNOWN, DBState.DEFAULT)
    }
}

@Serializable
data class CompletionStatData(val id: Uuid, val taskId: Uuid, val proposedDate: String,
    val completed: Boolean, val endDate: String
){
    constructor(completionStat: CompletionStat) : this(completionStat.id,completionStat.taskId,
        completionStat.proposedDate.toString(),completionStat.completed,completionStat.endDate.toString())

    fun parse(): CompletionStat {
        return CompletionStat(taskId, Instant.parse(proposedDate), completed, Instant.parse(endDate), id, DBState.UNKNOWN, DBState.DEFAULT)
    }
}
object SupabaseDBManager: DBManager {
    private lateinit var postgrest: Postgrest
    private val _ready = MutableStateFlow(false)
    val ready = _ready.asStateFlow()

    fun initialize(postgrest: Postgrest) {
        this.postgrest = postgrest
        _ready.value = true
    }

    override suspend fun insert(objects: Sequence<DBObject>): Result<Unit> = runCatching {
        objects.forEach { insertDBObject(it) }
    }
    private suspend fun insertDBObject(obj: DBObject) {
        when (obj) {
            is User -> postgrest.from("user").insert(UserData(obj))
            is Group -> {
                postgrest.from("workgroup").insert(GroupData(obj))
                postgrest.from("workgroupuser").insert(GroupUserData.serializeFrom(obj))
            }
            is Topic -> postgrest.from("topic").insert(TopicData(obj))
            is Tag -> postgrest.from("tag").insert(TagData(obj))
            is Task -> {
                postgrest.from("task").insert(TaskData(obj))
                postgrest.from("tasktag").insert(TaskTagData.serializeFrom(obj))
                postgrest.from("assignusertask").insert(AssignuserTaskData.serializeFrom(obj))
            }
            is CompletionStat -> postgrest.from("taskcompletion").insert(CompletionStatData(obj))
        }
    }

    override suspend fun update(objects: Sequence<DBObject>): Result<Unit> = runCatching {
        objects.forEach { updateDBObject(it) }
    }

    private suspend fun updateDBObject(obj: DBObject) {
        when (obj) {
            is User -> postgrest.from("user").update(UserData(obj)) { filter { eq("id", obj.id) } }
            is Group -> {
                postgrest.from("workgroup").update(GroupData(obj)) { filter { eq("id", obj.id) } }
                postgrest.from("workgroupuser").update(GroupUserData.serializeFrom(obj)) { filter { eq("groupId", obj.id) } }
            }
            is Topic -> postgrest.from("topic").update(TopicData(obj)) { filter { eq("id", obj.id) } }
            is Tag -> postgrest.from("tag").update(TagData(obj)) { filter { eq("id", obj.id) } }
            is Task -> {
                postgrest.from("task").update(TaskData(obj)) { filter { eq("id", obj.id) } }
                postgrest.from("tasktag").update(TaskTagData.serializeFrom(obj)) { filter { eq("taskId", obj.id) } }
                postgrest.from("assignusertask").update(AssignuserTaskData.serializeFrom(obj)) { filter { eq("taskId", obj.id) } }
            }
            is CompletionStat -> postgrest.from("taskcompletion").update(CompletionStatData(obj)) { filter { eq("id", obj.id) } }
        }
    }

    override suspend fun delete(objects: Sequence<DBObject>): Result<Unit> = runCatching {
        objects.forEach { deleteDBObject(it) }
    }

    private suspend fun deleteDBObject(obj: DBObject) {
        when (obj) {
            is User -> postgrest.from("user").delete { filter { eq("id", obj.id) } }
            is Group -> {
                postgrest.from("workgroup").delete { filter { eq("id", obj.id) } }
                postgrest.from("workgroupuser").delete { filter { eq("groupId", obj.id) } }
            }
            is Topic -> postgrest.from("topic").delete { filter { eq("id", obj.id) } }
            is Tag -> postgrest.from("tag").delete{ filter { eq("id", obj.id) } }
            is Task -> {
                postgrest.from("task").delete{ filter { eq("id", obj.id) } }
                postgrest.from("tasktag").delete { filter { eq("taskId", obj.id) } }
                postgrest.from("assignusertask").delete { filter { eq("taskId", obj.id) } }
            }
            is CompletionStat -> postgrest.from("taskcompletion").delete { filter { eq("id", obj.id) } }
        }
    }

    override suspend fun topics(): Result<List<Topic>> = runCatching {
        return Result.success(postgrest.from("topic")
            .select(Columns.raw("id, groupId, name, color")) {
                filter {
                    eq("groupId", Store.currentGroup().id)
                }
            }.decodeList<TopicData>()
            .map(TopicData::parse))
    }

    override suspend fun tags(): Result<List<Tag>> = runCatching {
        return Result.success(postgrest.from("tag")
                .select(Columns.raw("id, topicId, name, topic!inner()")) {
                filter {
                    eq("topic.groupId", Store.currentGroup().id)
                }
            }.decodeList<TagData>()
            .map(TagData::parse))
    }

    override suspend fun tasks(): Result<List<Task>> = runCatching {
        val taskData = postgrest.from("task")
            .select(Columns.raw("id, topicId, groupId, name, description, time, interval, priority, isCompleted, topic!inner()")) {
                filter {
                    eq("topic.groupId", Store.currentGroup().id)
                }
            }.decodeList<TaskData>()
        val taskTags = postgrest.from("tasktag")
            .select(Columns.raw("taskId, tagId, topicId")) {
                filter {
                    isIn("taskId", taskData.map { it.id })
                }
            }.decodeList<TaskTagData>()
            .groupBy { it.taskId }
        val taskUser = postgrest.from("assignusertask")
            .select(Columns.raw("taskId, userId , groupId" )){
                filter {
                    isIn("taskId", taskData.map { it.id })
                }
            }.decodeList<AssignuserTaskData>()
            .groupBy { it.taskId }
        return Result.success(taskData.map { it.parse(TaskTagData.parse(taskTags[it.id]!!), AssignuserTaskData.parse(taskUser[it.id]!!)) })
    }

    override suspend fun groups(): Result<List<Group>> = runCatching {
        val groupData = postgrest.from("workgroup")
            .select(Columns.raw("id, name, description, workgroupuser!inner()")) {
                filter {
                    eq("workgroupuser.userId", Store.currentUser().id)
                }
            }.decodeList<GroupData>()
        val groupUsersData = postgrest.from("workgroupuser")
            .select(Columns.raw("groupId, userId, privilege")) {
                filter {
                    isIn("groupId", groupData.map { it.id } )
                }
            }.decodeList<GroupUserData>()
            .groupBy { it.groupId }
        return Result.success(groupData.map { it.parse(GroupUserData.parse(groupUsersData[it.id]!!)) })
    }

    override suspend fun users(): Result<List<User>> = runCatching {
        return Result.success(postgrest.from("user")
            .select(Columns.raw("id, name, workgroupuser!inner()")) {
                filter {
                    isIn("workgroupuser.groupId", Store.groups().map { it.id }.toList())
                }
            }.decodeList<UserData>()
            .map(UserData::parse)
        )
    }

    override suspend fun completionStats(): Result<List<CompletionStat>> = runCatching {
        return Result.success(postgrest.from("taskcompletion")
            .select(Columns.raw("id, taskId, completed, proposedDate, endDate, task!inner(), topic!inner()")) {
                filter {
                    eq("topic.groupId", Store.currentGroup().id)
                }
            }.decodeList<CompletionStatData>()
            .map(CompletionStatData::parse))
    }

    suspend fun getInviteCodes(group: Uuid): Result<List<InvitationsData>> = runCatching {
        return Result.success(postgrest.from("invitations")
            .select(Columns.raw("groupId, code, privilege")) {
                filter {
                    eq("groupId", group)
                }
            }.decodeList<InvitationsData>())
    }

    suspend fun setInviteCode(group: Uuid, privilege: Privilege, code: Int): Result<Unit> = runCatching {
        postgrest.from("invitations").upsert(InvitationsData(group,code,privilege.ordinal))
    }

    suspend fun removeCode(group: Uuid, privilege: Privilege): Result<Unit> = runCatching {
        postgrest.from("invitations").delete {
            filter {
                eq("groupId", group)
                eq("privilege", privilege.ordinal)
            }
        }
    }
}