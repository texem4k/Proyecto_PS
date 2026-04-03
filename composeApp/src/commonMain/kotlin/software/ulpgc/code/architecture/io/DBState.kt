package software.ulpgc.code.architecture.io

import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.Task
import software.ulpgc.code.architecture.model.Topic

enum class DBState {
    DEFAULT, NEW, UPDATED, DELETED;
    fun isNew(topic: Topic): Boolean {
        return topic.dbState == NEW
    }
    fun isNew(tag: Tag): Boolean {
        return tag.dbState == NEW
    }
    fun isNew(task: Task): Boolean {
        return task.dbState == NEW
    }

    fun isUpdated(topic: Topic): Boolean {
        return topic.dbState == UPDATED
    }
    fun isUpdated(tag: Tag): Boolean {
        return tag.dbState == UPDATED
    }
    fun isUpdated(task: Task): Boolean {
        return task.dbState == UPDATED
    }
    fun isDeleted(topic: Topic): Boolean {
        return topic.dbState == DELETED
    }
    fun isDeleted(tag: Tag): Boolean {
        return tag.dbState == DELETED
    }
    fun isDeleted(task: Task): Boolean {
        return task.dbState == DELETED
    }
}