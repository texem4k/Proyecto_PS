package software.ulpgc.code.application.ui.pages.Calendar

import androidx.compose.ui.graphics.Color
import software.ulpgc.code.architecture.model.tasks.Task

data class SampleEntry(
    val title: String,
    val time: String,
    val color: Color,
    val task: Task? = null
)
