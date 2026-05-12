package software.ulpgc.code.architecture.io

import software.ulpgc.code.architecture.model.Group
import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.Topic
import software.ulpgc.code.architecture.model.User
import software.ulpgc.code.architecture.model.tasks.CompletionStat

interface Storage {
    fun currentGroup(): Group
    fun changeGroupTo(group: Group)

    fun topics(): Sequence<Topic>
    fun tags(): Sequence<Tag>
    fun tasks(): Sequence<Task>
    fun groups(): Sequence<Group>
    fun users(): Sequence<User>
    fun completions(): Sequence<CompletionStat>

    fun addTopic(topic: Topic)
    fun addTag(tag: Tag)
    fun addTask(task: Task)
    fun addGroup(group: Group)
    fun addUser(user: User)
    fun addCompletionStat(completionStat: CompletionStat)
}