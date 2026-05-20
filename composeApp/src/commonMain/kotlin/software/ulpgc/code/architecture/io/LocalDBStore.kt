package software.ulpgc.code.architecture.io

import software.ulpgc.code.architecture.control.coroutines.Coroutinable
import software.ulpgc.code.architecture.control.coroutines.CoroutineManager
import software.ulpgc.code.architecture.control.exceptions.AppException
import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.model.Group
import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.Topic
import software.ulpgc.code.architecture.model.User
import software.ulpgc.code.architecture.model.tasks.CompletionStat
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.uuid.Uuid

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
        this.dbObjects = { dbObjects().filterNot { it.isLocalDisabled() } }
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
            manager.users().getOrThrow().forEach {
                disableIfRoot(it)
                Store.add(it)
            }
            manager.groups().getOrThrow().forEach {
                disableIfRoot(it)
                Store.add(it)
            }
            manager.topics().getOrThrow().forEach{
                disableIfRoot(it)
                Store.add(it)
            }
            manager.tags().getOrThrow().forEach {
                disableIfRoot(it)
                Store.add(it)
            }
            manager.tasks().getOrThrow().forEach {
                disableIfRoot(it)
                Store.add(it)
            }
            manager.completionStats().getOrThrow().forEach {
                disableIfRoot(it)
                Store.add(it)
            }
        } catch (e: AppException) {
            onFailLoad(e)
        }
    }

    private val rootId = Uuid.parse("00000000-0000-0000-0000-000000000000")
    private fun <T: DBObject> disableIfRoot(obj: T) {
        when (obj) {
            is Group -> {
                if (obj.id == rootId) obj.cloudDBState = DBState.DISABLED
            }
            is User -> {
                if (obj.id == rootId) obj.cloudDBState = DBState.DISABLED
            }
            is Topic -> {
                if (obj.groupId == rootId) obj.cloudDBState = DBState.DISABLED
            }
            is Tag -> {
                if (Store.topics().find { it.id == obj.topicId }?.id == rootId) obj.cloudDBState = DBState.DISABLED
            }
            is Task -> {
                if (Store.topics().find { it.id == obj.topicId }?.id == rootId) obj.cloudDBState = DBState.DISABLED
            }
            is CompletionStat -> {
                val topicId = Store.tasks().find { it.id == obj.taskId }?.id
                if (Store.topics().find { it.id == topicId }?.id == rootId) obj.cloudDBState = DBState.DISABLED
            }
        }

    }

    override suspend fun execute() {
        deleteRequired(dbObjects().filter { it.localDBState == DBState.DELETED })
        updateRequired(dbObjects().filter { it.isLocalUpdated() })
        insertRequired(dbObjects().filter { it.isLocalNew() })
    }

    override suspend fun onDispose() {
        execute()
        LogMaster.log("Parando guardado automático")
    }
}