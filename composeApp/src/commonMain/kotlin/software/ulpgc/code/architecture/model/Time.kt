package software.ulpgc.code.architecture.model
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class Time(
    var id: Uuid,
    var type: String,
    var start: Instant,
    var end: Instant
)