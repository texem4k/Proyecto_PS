package software.ulpgc.code.application.io.localDB

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import proyecto_ps.composeapp.generated.resources.Res
import software.ulpgc.code.architecture.io.DBObject
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

private val json = Json { ignoreUnknownKeys = true }

@Serializable
data class UserData(val id: Uuid, val name: String)

@Serializable
data class TopicData(val id: Uuid, val name: String, val groupId: Uuid, val color: Int)

@Serializable
data class TagData(val id: Uuid, val name: String, val topicId: Uuid)

@Serializable
data class TimeData(val id: Uuid, val type: Int, val start: String, val end: String)

@Serializable
data class GroupUser(val id: Uuid, val privilege: Privilege)

@Serializable
data class GroupData(val id: Uuid, val name: String, val description: String, val users: List<GroupUser>)

@Serializable
data class TaskData(
    val id: Uuid,
    val priority: Int,
    val name: String,
    val users: List<Uuid>,
    val description: String,
    val interval: Int,
    val topicId: Uuid,
    val tags: List<Uuid>,
    val time: TimeData
)

@Serializable
data class StatData(
    val id: Uuid,
    val taskId: Uuid,
    val proposedDate: String,
    val completed: Boolean,
    val endDate: String
)

@Serializable
data class DBData(
    val users: List<UserData>,
    val topics: List<TopicData>,
    val tags: List<TagData>,
    val tasks: List<TaskData>,
    val groups: List<GroupData>,
    val stats: List<StatData>
) {
    fun dbObjects(): Sequence<DBObject> = userSequence() + groupSequence() + topicSequence() + tagSequence() + taskSequence() + statSequence()

    private fun statSequence(): Sequence<DBObject> =
        this.stats.asSequence().map { (id, taskId, proposedDate, completed, endDate) -> CompletionStat(taskId, Instant.parse(proposedDate), completed,
            Instant.parse(endDate), id) }

    private fun topicSequence(): Sequence<Topic> =
        this.topics.asSequence().map { (id, name, groupId, color) -> Topic(name, Color(color), groupId, id) }

    private fun tagSequence(): Sequence<Tag> =
        this.tags.asSequence().map { (id, name, topicId) -> Tag(name, topicId, id) }

    private fun taskSequence(): Sequence<Task> =
        this.tasks.asSequence().map { (id, priority, name, users, description, interval, topicId, tags, time) ->
            Task(
                topicId,
                name,
                description,
                TimeFactory.createTime(Instant.parse(time.start), Instant.parse(time.end), time.type, time.id),
                TaskInterval.entries[interval],
                Priority.fromValue(priority),
                tags.toMutableSet(),
                users.toMutableSet(),
                false,
                id
            )
        }

    private fun groupSequence(): Sequence<Group> =
        this.groups.asSequence().map { (id, name, description, users) ->
            Group(
                name,
                description,
                users.associate { it.id to it.privilege }.toMutableMap(),
                id
            )
        }

    private fun userSequence(): Sequence<User> =
        this.users.asSequence().map { (id, name) ->
            User(name,id)
        }
}

class JSONParser {
    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadDBData(src: String): DBData {
        val bytes = Res.readBytes("files/$src")
        return json.decodeFromString<DBData>(bytes.decodeToString())
    }
}