package software.ulpgc.code

import software.ulpgc.code.architecture.control.CommandBuilder
import software.ulpgc.code.architecture.control.CommandType
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.Topic
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.tasks.TaskInterval
import software.ulpgc.code.architecture.model.times.TimeFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Clock
import kotlin.uuid.Uuid

private fun builder(store: MockStore) = CommandBuilder(store)

private fun makeTime() = TimeFactory().createTime(Clock.System.now(), 1)

private fun makeTask(store: Storage) = Task(
    priority = 1,
    name = "Original Task",
    userId = Uuid.random(),
    description = "desc",
    topicId = Uuid.random(),
    time = makeTime(),
    interval = TaskInterval.NONE,
    tags = mutableListOf()
).also { store.addTasks(listOf(it)) }

private fun makeTopic(store: Storage) =
    Topic("Original Topic", 123456).also { store.addTopics(listOf(it)) }

private fun makeTag(store: Storage, topicId: Uuid = Uuid.random()) =
    Tag("Original Tag", topicId).also { store.addTags(listOf(it)) }

class CreateTopicCommandTest {

    @Test
    fun `execute - adds topic to store`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()

        launcher.launch(
            builder(store).set("name", "Work").set("color", "123456")
                .build(CommandType.CREATE_TOPIC)
        )

        assertEquals(1, store.topics().count())
        assertEquals("Work", store.topics().first().name)
    }

    @Test
    fun `undo after create - removes topic`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()


        launcher.launch(
            builder(store).set("name", "Work").set("color", "123456")
                .build(CommandType.CREATE_TOPIC)
        )
        launcher.undo()

        assertEquals(0, store.topics().count())
    }

    @Test
    fun `redo after undo create - topic visible again`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()


        launcher.launch(
            builder(store).set("name", "Work").set("color", "123456")
                .build(CommandType.CREATE_TOPIC)
        )
        launcher.undo()
        launcher.redo()

        assertEquals(1, store.topics().count())
    }
}

class UpdateTopicCommandTest {

    @Test
    fun `execute - updates topic name and color`() {
        val store =MockStore()
        val launcher = MockCommandLauncher()

        val topic = makeTopic(store)

        launcher.launch(
            builder(store)
                .set("id", topic.id.toString())
                .set("name", "Updated Topic")
                .set("color", "123456")
                .build(CommandType.UPDATE_TOPIC)
        )

        assertEquals("Updated Topic", topic.name)
        assertEquals(123456, topic.color)
        assertEquals(DBState.UPDATED, topic.dbState)
    }

    @Test
    fun `undo after update - restores original name and color`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()

        val topic = makeTopic(store)

        launcher.launch(
            builder(store)
                .set("id", topic.id.toString())
                .set("name", "Updated Topic")
                .set("color", "123456")
                .build(CommandType.UPDATE_TOPIC)
        )
        launcher.undo()

        assertEquals("Original Topic", topic.name)
        assertEquals(123456, topic.color)
    }

    @Test
    fun `redo after undo update - re-applies changes`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()

        val topic = makeTopic(store)

        launcher.launch(
            builder(store)
                .set("id", topic.id.toString())
                .set("name", "Updated Topic")
                .set("color", "123456")
                .build(CommandType.UPDATE_TOPIC)
        )
        launcher.undo()
        launcher.redo()

        assertEquals("Updated Topic", topic.name)
        assertEquals(123456, topic.color)
    }
}

class DeleteTopicCommandTest {

    @Test
    fun `execute - topic no longer visible in store`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()
        val topic = makeTopic(store)

        launcher.launch(
            builder(store).set("id", topic.id.toString())
                .build(CommandType.DELETE_TOPIC)
        )

        assertEquals(0, store.topics().count())
        assertEquals(DBState.DELETED, topic.dbState)
    }

    @Test
    fun `undo after delete - topic visible again`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()

        val topic = makeTopic(store)

        launcher.launch(
            builder(store).set("id", topic.id.toString())
                .build(CommandType.DELETE_TOPIC)
        )
        launcher.undo()

        assertEquals(1, store.topics().count())
    }

    @Test
    fun `redo after undo delete - topic deleted again`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()

        val topic = makeTopic(store)

        launcher.launch(
            builder(store).set("id", topic.id.toString())
                .build(CommandType.DELETE_TOPIC)
        )
        launcher.undo()
        launcher.redo()

        assertEquals(0, store.topics().count())
    }
}

// ═════════════════════════════════════════════
// TAG COMMANDS
// ═════════════════════════════════════════════
class CreateTagCommandTest {

    @Test
    fun `execute - adds tag to store`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()


        launcher.launch(
            builder(store).set("name", "kotlin").set("topicId", Uuid.random().toString())
                .build(CommandType.CREATE_TAG)
        )

        assertEquals(1, store.tags().count())
        assertEquals("kotlin", store.tags().first().name)
    }

    @Test
    fun `undo after create - removes tag`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()


        launcher.launch(
            builder(store).set("name", "kotlin").set("topicId", Uuid.random().toString())
                .build(CommandType.CREATE_TAG)
        )
        launcher.undo()

        assertEquals(0, store.tags().count())
    }

    @Test
    fun `redo after undo create - tag visible again`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()


        launcher.launch(
            builder(store).set("name", "kotlin").set("topicId", Uuid.random().toString())
                .build(CommandType.CREATE_TAG)
        )
        launcher.undo()
        launcher.redo()

        assertEquals(1, store.tags().count())
    }
}

class UpdateTagCommandTest {

    @Test
    fun `execute - updates tag name and topicId`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()

        val newTopicId = Uuid.random()
        val tag = makeTag(store)

        launcher.launch(
            builder(store)
                .set("id", tag.id.toString())
                .set("name", "updated-tag")
                .set("topicId", newTopicId.toString())
                .build(CommandType.UPDATE_TAG)
        )

        assertEquals("updated-tag", tag.name)
        assertEquals(newTopicId, tag.topicId)
        assertEquals(DBState.UPDATED, tag.dbState)
    }

    @Test
    fun `undo after update - restores original name and topicId`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()

        val originalTopicId = Uuid.random()
        val tag = makeTag(store, topicId = originalTopicId)

        launcher.launch(
            builder(store)
                .set("id", tag.id.toString())
                .set("name", "updated-tag")
                .set("topicId", Uuid.random().toString())
                .build(CommandType.UPDATE_TAG)
        )
        launcher.undo()

        assertEquals("Original Tag", tag.name)
        assertEquals(originalTopicId, tag.topicId)
    }

    @Test
    fun `redo after undo update - re-applies changes`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()

        val newTopicId = Uuid.random()
        val tag = makeTag(store)

        launcher.launch(
            builder(store)
                .set("id", tag.id.toString())
                .set("name", "updated-tag")
                .set("topicId", newTopicId.toString())
                .build(CommandType.UPDATE_TAG)
        )
        launcher.undo()
        launcher.redo()

        assertEquals("updated-tag", tag.name)
        assertEquals(newTopicId, tag.topicId)
    }
}

class DeleteTagCommandTest {

    @Test
    fun `execute - tag no longer visible in store`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()

        val tag = makeTag(store)

        launcher.launch(
            builder(store).set("id", tag.id.toString())
                .build(CommandType.DELETE_TAG)
        )

        assertEquals(0, store.tags().count())
        assertEquals(DBState.DELETED, tag.dbState)
    }

    @Test
    fun `undo after delete - tag visible again`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()

        val tag = makeTag(store)

        launcher.launch(
            builder(store).set("id", tag.id.toString())
                .build(CommandType.DELETE_TAG)
        )
        launcher.undo()

        assertEquals(1, store.tags().count())
    }

    @Test
    fun `redo after undo delete - tag deleted again`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()

        val tag = makeTag(store)

        launcher.launch(
            builder(store).set("id", tag.id.toString())
                .build(CommandType.DELETE_TAG)
        )
        launcher.undo()
        launcher.redo()

        assertEquals(0, store.tags().count())
    }
}

// ═════════════════════════════════════════════
// TASK COMMANDS
// ═════════════════════════════════════════════
class CreateTaskCommandTest {

    @Test
    fun `execute - adds task to store`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()


        launcher.launch(
            builder(store)
                .set("name", "My Task")
                .set("priority", "1")
                .set("userId", Uuid.random().toString())
                .set("description", "desc")
                .set("topicId", Uuid.random().toString())
                .set("interval", TaskInterval.NONE.name)
                .build(CommandType.CREATE_TASK)
        )

        assertEquals(1, store.tasks().count())
        assertEquals("My Task", store.tasks().first().name)
    }

    @Test
    fun `undo after create - task removed`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()


        launcher.launch(
            builder(store)
                .set("name", "My Task")
                .set("priority", "1")
                .set("userId", Uuid.random().toString())
                .set("description", "desc")
                .set("topicId", Uuid.random().toString())
                .set("interval", TaskInterval.NONE.name)
                .build(CommandType.CREATE_TASK)
        )
        launcher.undo()

        assertEquals(0, store.tasks().count())
    }

    @Test
    fun `redo after undo create - task visible again`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()


        launcher.launch(
            builder(store)
                .set("name", "My Task")
                .set("priority", "1")
                .set("userId", Uuid.random().toString())
                .set("description", "desc")
                .set("topicId", Uuid.random().toString())
                .set("interval", TaskInterval.NONE.name)
                .build(CommandType.CREATE_TASK)
        )
        launcher.undo()
        launcher.redo()

        assertEquals(1, store.tasks().count())
    }
}

class UpdateTaskCommandTest {

    @Test
    fun `execute - updates all task fields`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()

        val task = makeTask(store)
        val newTopicId = Uuid.random()

        launcher.launch(
            builder(store)
                .set("id", task.id.toString())
                .set("name", "Updated Task")
                .set("priority", "2")
                .set("description", "new desc")
                .set("topicId", newTopicId.toString())
                .set("interval", TaskInterval.DAY.name)
                .build(CommandType.UPDATE_TASK)
        )

        assertEquals("Updated Task",     task.name)
        assertEquals(2,                  task.priority)
        assertEquals("new desc",         task.description)
        assertEquals(newTopicId,         task.topicId)
        assertEquals(TaskInterval.DAY,   task.interval)
        assertEquals(DBState.UPDATED,    task.dbState)
    }

    @Test
    fun `undo after update - restores original task fields`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()

        val task = makeTask(store)
        val originalTopicId = task.topicId

        launcher.launch(
            builder(store)
                .set("id", task.id.toString())
                .set("name", "Updated Task")
                .set("priority", "2")
                .set("description", "new desc")
                .set("topicId", Uuid.random().toString())
                .set("interval", TaskInterval.DAY.name)
                .build(CommandType.UPDATE_TASK)
        )
        launcher.undo()

        assertEquals("Original Task",   task.name)
        assertEquals(1,                 task.priority)
        assertEquals("desc",            task.description)
        assertEquals(originalTopicId,   task.topicId)
        assertEquals(TaskInterval.NONE, task.interval)
    }

    @Test
    fun `redo after undo update - re-applies changes`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()

        val task = makeTask(store)

        launcher.launch(
            builder(store)
                .set("id", task.id.toString())
                .set("name", "Updated Task")
                .set("priority", "2")
                .set("description", "new desc")
                .set("topicId", Uuid.random().toString())
                .set("interval", TaskInterval.DAY.name)
                .build(CommandType.UPDATE_TASK)
        )
        launcher.undo()
        launcher.redo()

        assertEquals("Updated Task", task.name)
        assertEquals(2,              task.priority)
    }
}

class DeleteTaskCommandTest {

    @Test
    fun `execute - task no longer visible in store`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()
        val task = makeTask(store)

        launcher.launch(
            builder(store).set("id", task.id.toString())
                .build(CommandType.DELETE_TASK)
        )

        assertEquals(0, store.tasks().count())
        assertEquals(DBState.DELETED, task.dbState)
    }

    @Test
    fun `undo after delete - task visible again`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()
        val task = makeTask(store)

        launcher.launch(
            builder(store).set("id", task.id.toString())
                .build(CommandType.DELETE_TASK)
        )
        launcher.undo()

        assertEquals(1, store.tasks().count())
    }

    @Test
    fun `redo after undo delete - task deleted again`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()
        val task = makeTask(store)

        launcher.launch(
            builder(store).set("id", task.id.toString())
                .build(CommandType.DELETE_TASK)
        )
        launcher.undo()
        launcher.redo()

        assertEquals(0, store.tasks().count())
    }
}

// ═════════════════════════════════════════════
// COMMAND HISTORY EDGE CASES
// ═════════════════════════════════════════════
class CommandLauncherHistoryTest {

    @Test
    fun `new command after undo discards redo history`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()

        launcher.launch(builder(store).set("name", "A").set("color", "123456").build(CommandType.CREATE_TOPIC))
        launcher.launch(builder(store).set("name", "B").set("color", "123456").build(CommandType.CREATE_TOPIC))
        launcher.undo()
        launcher.launch(builder(store).set("name", "C").set("color", "123456").build(CommandType.CREATE_TOPIC))

        assertFailsWith<Throwable> { launcher.redo() }
    }

    @Test
    fun `undo past beginning throws`() {
        val launcher = MockCommandLauncher()
        assertFailsWith<Throwable> { launcher.undo() }
    }

    @Test
    fun `redo past end throws`() {
        val store = MockStore()
        val launcher = MockCommandLauncher()

        launcher.launch(builder(store).set("name", "A").set("color", "123456").build(CommandType.CREATE_TOPIC))
        assertFailsWith<Throwable> { launcher.redo() }
    }
}