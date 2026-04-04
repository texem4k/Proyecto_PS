package software.ulpgc.code.application.io

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import proyecto_ps.composeapp.generated.resources.Res
import software.ulpgc.code.architecture.io.DBObject
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.Topic
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.tasks.TaskInterval
import software.ulpgc.code.architecture.model.times.TimeFactory
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val json = Json { ignoreUnknownKeys = true }

class JSONParser {
    @Serializable
    data class UserData(val id: Uuid, val name: String)

    @Serializable
    data class TopicData(val id: Uuid, val name: String, val color: Int)

    @Serializable
    data class TagData(val id: Uuid, val name: String, val topicId: Uuid)

    @Serializable
    data class TaskData(val id: Uuid, val priority: Int, val name: String, val userId: Uuid,
                        val description: String, val interval: Int, val topicId: Uuid, val tags: List<Uuid>, val time: TimeData)

    @Serializable
    data class TimeData(val id: Uuid, val type: Int, val start: Instant, val end: Instant)

    @Serializable
    data class DBData(
        @SerialName("Users") val users: List<UserData>,
        @SerialName("Topics") val topics: List<TopicData>,
        @SerialName("Tags") val tags: List<TagData>,
        @SerialName("Tasks") val tasks: List<TaskData>
    ) {
        fun dbObjects(): Sequence<DBObject> = topicSequence() + tagSequence() + taskSequence()

        private fun taskSequence(): Sequence<Task> {
            return tasks.asSequence().map {
                (id, priority, name, userId, description, interval, topicId, tags, time) ->
                    Task(
                        priority,
                        name,
                        userId,
                        description,
                        topicId,
                        TimeFactory().createTime(time.start, time.end, time.type, id),
                        TaskInterval.entries[interval],
                        tags.toMutableList(),
                        id,
                        DBState.DEFAULT
                    )
            }
        }

        private fun topicSequence(): Sequence<Topic> = topics.asSequence().map { (id, name, color) -> Topic(name, color, id, DBState.DEFAULT) }

        private fun tagSequence(): Sequence<Tag> = tags.asSequence().map { (id, name, topicId) -> Tag(name, topicId, id, DBState.DEFAULT) }




    }

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadDBData(src: String): DBData {
        val bytes = Res.readBytes("files/$src")
        return json.decodeFromString<DBData>(bytes.decodeToString())
    }
}