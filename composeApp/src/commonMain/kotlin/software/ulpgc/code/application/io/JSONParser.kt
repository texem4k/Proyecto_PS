package software.ulpgc.code.application.io

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import proyecto_ps.composeapp.generated.resources.Res

private val json = Json { ignoreUnknownKeys = true }

class JSONParser {
    @Serializable
    data class UserData(val id: Int, val name: String)

    @Serializable
    data class TopicData(val id: Int, val name: String, val color: Int)

    @Serializable
    data class TagData(val id: Int, val name: String, val topicId: Int)

    @Serializable
    data class TaskData(val id: Int, val name: String)

    @Serializable
    data class DBData(
        @SerialName("Users") val users: List<UserData>,
        @SerialName("Topics") val topics: List<TopicData>,
        @SerialName("Tags") val tags: List<TagData>,
        @SerialName("Tasks") val tasks: List<TaskData>
    )

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadDBData(src: String): DBData {
        val bytes = Res.readBytes("files/$src")
        return json.decodeFromString<DBData>(bytes.decodeToString())
    }
}