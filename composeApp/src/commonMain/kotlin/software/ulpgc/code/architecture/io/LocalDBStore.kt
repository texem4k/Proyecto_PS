package software.ulpgc.code.architecture.io

import software.ulpgc.code.architecture.control.coroutines.Coroutinable
import software.ulpgc.code.architecture.control.coroutines.CoroutineManager
import software.ulpgc.code.architecture.control.exceptions.AppException
import software.ulpgc.code.architecture.control.logs.LogMaster

object LocalDBStore: Coroutinable {
    lateinit var cleanLists: () -> Unit
    lateinit var afterLoad: () -> Unit
    lateinit var onFailLoad: (AppException) -> Unit
    lateinit var dbObjects: () -> Sequence<DBObject>
    lateinit var manager: DBManager

    fun initialize(manager: DBManager, cleanLists: () -> Unit, afterLoad: () -> Unit, onFailLoad: (AppException) -> Unit, dbObjects: () -> Sequence<DBObject>) {
        this.cleanLists = cleanLists
        this.manager = manager
        this.afterLoad = afterLoad
        this.onFailLoad = onFailLoad
        this.dbObjects = dbObjects
        CoroutineManager.add(this)
    }
    
    private suspend fun insertRequired(objects: Sequence<DBObject>) {
        manager.insert(objects)
        objects.forEach { it.localDBState = DBState.DEFAULT }
    }

    private suspend fun updateRequired(objects: Sequence<DBObject>) {
        manager.update(objects)
        objects.forEach { it.localDBState = DBState.DEFAULT }
    }

    private suspend fun deleteRequired(objects: Sequence<DBObject>) {
        manager.delete(objects)
        objects.forEach{ it.localDBState = DBState.CLEARED }
        cleanLists()
    }

    override val delayMilis: Long = 30_000L

    override suspend fun onInit() {
        LogMaster.log("Cargando datos BD")
        loadDBData()
        LogMaster.log("Finalizado carga de datos BD")
        afterLoad()
    }

    private suspend fun loadDBData() {
        try {
            manager.users().getOrThrow().forEach { Store.addUser(it) }
            manager.groups().getOrThrow().forEach { Store.addGroup(it) }
            manager.topics().getOrThrow().forEach{ Store.addTopic(it) }
            manager.tags().getOrThrow().forEach { Store.addTag(it) }
            manager.tasks().getOrThrow().forEach { Store.addTask(it) }
            manager.completionStats().getOrThrow().forEach { Store.addCompletionStat(it)}
        } catch (e: AppException) {
            onFailLoad(e)
        }
    }

    override suspend fun execute() {
        deleteRequired(dbObjects().filter { it.isLocalDeleted() })
        updateRequired(dbObjects().filter { it.isLocalUpdated() })
        insertRequired(dbObjects().filter { it.isLocalNew() })
    }

    override suspend fun onDispose() {
        execute()
        LogMaster.log("Parando guardado automático")
    }
}