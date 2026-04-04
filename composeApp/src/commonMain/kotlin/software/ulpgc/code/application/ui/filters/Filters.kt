package software.ulpgc.code.application.ui.filters

data class TaskFilters(
    var hasFilter : Boolean = false,
    var status: Set<String> = emptySet(),
    var priority: Set<String> = emptySet(),
    var topics: Set<String> = emptySet(),
)