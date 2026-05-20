package software.ulpgc.code.architecture.io

import software.ulpgc.code.architecture.control.coroutines.Coroutinable
import software.ulpgc.code.architecture.control.coroutines.CoroutineManager
import software.ulpgc.code.architecture.control.logs.LogMaster

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
        this.dbObjects = dbObjects
        this.canUseDatabase = canUseDatabase
        CoroutineManager.add(this)
    }

    private suspend fun insertRequired(objects: Sequence<DBObject>) {
        manager.insert(objects)
        objects.forEach { it.cloudDBState = DBState.DEFAULT }
    }

    private suspend fun updateRequired(objects: Sequence<DBObject>) {
        manager.update(objects)
        objects.forEach { it.cloudDBState = DBState.DEFAULT }
    }

    private suspend fun deleteRequired(objects: Sequence<DBObject>) {
        manager.delete(objects)
        objects.forEach { it.cloudDBState = DBState.CLEARED }
        cleanLists()
    }

    override val delayMilis: Long = 1_000L

    override suspend fun onInit() {
        LogMaster.log("Iniciando Cloud DB Store")
    }

    private suspend fun loadDBData() {
        manager.groups().getOrThrow().forEach { Store.addGroup(it) }
        manager.users().getOrThrow().forEach { Store.addUser(it) }
        manager.topics().getOrThrow().forEach { Store.addTopic(it) }
        manager.tags().getOrThrow().forEach { Store.addTag(it) }
        manager.tasks().getOrThrow().forEach { Store.addTask(it) }
        manager.completionStats().getOrThrow().forEach { Store.addCompletionStat(it) }
    }

    override suspend fun execute() {
        if( !canUseDatabase()) return
        deleteRequired(dbObjects().filter { it.cloudDBState == DBState.DELETED })
        updateRequired(dbObjects().filter { it.isCloudUpdated() })
        insertRequired(dbObjects().filter { it.isCloudNew() })
        loadDBData()
    }

    override suspend fun onDispose() {
        execute()
        LogMaster.log("Parando guardado automático")
    }
}