package software.ulpgc.code

import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.io.isDeleted
import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.Topic
import software.ulpgc.code.architecture.model.tasks.Task

class MockStore: Storage {
    private val topics: MutableSet<Topic> = mutableSetOf()
    private val tags:   MutableSet<Tag>   = mutableSetOf()
    private val tasks:  MutableSet<Task>  = mutableSetOf()

    override fun topics(): Sequence<Topic> = topics.asSequence().filter { !it.isDeleted() }
    override fun tags():   Sequence<Tag>   = tags.asSequence().filter   { !it.isDeleted() }
    override fun tasks():  Sequence<Task>  = tasks.asSequence().filter  { !it.isDeleted() }

    override fun addTopics(topics: List<Topic>) { this.topics.addAll(topics) }
    override fun addTags(tags: List<Tag>)       { this.tags.addAll(tags)     }
    override fun addTasks(tasks: List<Task>)    { this.tasks.addAll(tasks)   }
}