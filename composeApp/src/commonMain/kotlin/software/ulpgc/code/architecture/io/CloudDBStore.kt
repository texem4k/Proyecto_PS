package software.ulpgc.code.architecture.io

import software.ulpgc.code.architecture.control.coroutines.Coroutinable
import software.ulpgc.code.architecture.control.coroutines.CoroutineManager
import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.model.Group
import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.Topic
import software.ulpgc.code.architecture.model.User
import software.ulpgc.code.architecture.model.tasks.CompletionStat
import software.ulpgc.code.architecture.model.tasks.Task

object CloudDBStore: Coroutinable {
    lateinit var cleanLists: () -> Unit
    lateinit var dbObjects: () -> Sequence<DBObject>
    lateinit var manager: DBManager
    lateinit var canUseDatabase: () -> Boolean

    fun initialize(
        manager: DBManager,
        cleanLists: () -> Unit,
        dbObjects: () -> Sequence<DBObject>,
        canUseDatabase: () -> Boolean
    ) {
        this.manager = manager
        this.cleanLists = cleanLists
        this.dbObjects = { dbObjects().filterNot { it.isCloudDisabled() } }
        this.canUseDatabase = canUseDatabase
        CoroutineManager.add(this)
    }

    private suspend fun insertRequired(objects: Sequence<DBObject>) {
        manager.insert(objects).getOrThrow()
        objects.forEach { it.cloudDBState = DBState.DEFAULT }
    }

    private suspend fun updateRequired(objects: Sequence<DBObject>) {
        manager.update(objects).getOrThrow()
        objects.forEach { it.cloudDBState = DBState.DEFAULT }
    }

    private suspend fun deleteRequired(objects: Sequence<DBObject>) {
        manager.delete(objects).getOrThrow()
        objects.forEach { it.cloudDBState = DBState.CLEARED }
        cleanLists()
    }

    override val delayMilis: Long = 1_000L

    override suspend fun onInit() {
        LogMaster.log("Iniciando Cloud DB Store")
    }

    private suspend fun loadDBData() {
        insertOrUpdate(manager.groups().getOrThrow().asSequence())
        insertOrUpdate(manager.users().getOrThrow().asSequence())
        insertOrUpdate(manager.topics().getOrThrow().asSequence())
        insertOrUpdate(manager.tags().getOrThrow().asSequence())
        insertOrUpdate(manager.tasks().getOrThrow().asSequence())
        insertOrUpdate(manager.completionStats().getOrThrow().asSequence())
    }

    private fun <T: DBObject> insertOrUpdate(objects: Sequence<T>) {
        objects.forEach {
            val obj = Store.tryFind(it)
            if (obj == null) {
                Store.add(it)
                println("puta base de dwatos añade")
            }
            else if (obj != it) {
                println(obj != it)
                update(obj, it)
                println(obj)
                println(it)
            }
            else obj.cloudDBState = DBState.DEFAULT
        }
    }

    private fun <T: DBObject> update(original: T, new: T) {
        when (original) {
            is Group -> {
                val new = new as Group
                original.name = new.name
                original.description = new.description
                original.users = new.users
            }
            is User -> {
                val new = new as User
                original.name = new.name
            }
            is Topic -> {
                val new = new as Topic
                original.name = new.name
                original.color = new.color
            }
            is Tag -> {
                val new = new as Tag
                original.name = new.name
            }
            is Task -> {
                val new = new as Task
                original.name = new.name
                original.description = new.description
                original.time = new.time
                original.interval = new.interval
                original.priority = new.priority
                original.tags = new.tags
                original.users = new.users
                original.isCompleted = new.isCompleted
            }
            is CompletionStat -> {
                val new = new as CompletionStat
                original.completed = new.completed
                original.endDate = new.endDate
            }
        }
        original.cloudDBState = DBState.DEFAULT
        original.localDBState = DBState.UNKNOWN
        Store.refresh()
    }


    override suspend fun execute() {
        if( !canUseDatabase()) return
        deleteRequired(dbObjects().filter { it.cloudDBState == DBState.DELETED })
        updateRequired(dbObjects().filter { it.isCloudUpdated() })
        insertRequired(dbObjects().filter { it.isCloudNew() })
        Store.clear()
        loadDBData()
    }

    override suspend fun onDispose() {
        execute()
        LogMaster.log("Parando guardado automático")
    }
}