package com.example.runpal.repositories.run

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.runpal.NotFound
import com.example.runpal.R
import com.example.runpal.ServerException
import com.example.runpal.models.Run
import com.example.runpal.models.RunData
import com.example.runpal.repositories.LoginManager
import com.example.runpal.room.Sync
import com.example.runpal.room.SyncDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

/**
 * This class is designed to work with both repositories,
 * preferring one or the other depending on context, and synchronizing data when useful.
 */
@Singleton
class CombinedRunRepository @Inject constructor(
    private val localRunRepository: LocalRunRepository,
    private val serverRunRepository: ServerRunRepository,
    private val loginManager: LoginManager,
    private val syncDao: SyncDao,
    @ApplicationContext private val context: Context
): RunRepository {

    private val unsynced: MutableMap<Pair<String, Long>, Long?> = mutableMapOf()
    private val syncing: MutableMap<Pair<String, Long>, Unit> = mutableMapOf()
    init {
        CoroutineScope(Dispatchers.Main).launch {
            for (sync in syncDao.getAll())
                unsynced.put(sync.user to sync.runId, sync.since)
        }
    }
    private suspend fun unsyncedCreate(run: Run) {
        val key = run.user to run.id
        unsynced[key] = null
        syncDao.insert(Sync(user = run.user, runId = run.id, since = null))
    }
    private suspend fun unsyncedUpdate(runData: RunData) {
        val run = runData.run
        val key = run.user to run.id
        if (unsynced.containsKey(key)) return
        val since = if (runData.path.size > 0) runData.path[0].time - 1 else System.currentTimeMillis()
        unsynced[key] = since
        syncDao.insert(Sync(user = run.user, runId = run.id, since = since))
    }
    private fun isUnsynced(run: Run) = unsynced.contains(run.user to run.id)
    private suspend fun attemptSync(run: Run) {
        val key = run.user to run.id
        if (!unsynced.containsKey(key) || syncing[key] != null) return
        syncing[key] = Unit
        val update = localRunRepository.getUpdate(user = run.user, id = run.id, since = unsynced[key] ?: 0L)
        try {
            if (unsynced[key] == null) {
                serverRunRepository.create(update.run)
                unsynced[key] = 0L
                syncDao.update(Sync(user = run.user, runId = run.id, since = 0L))
            }
            serverRunRepository.update(update)
            syncDao.delete(Sync(user = run.user, runId = run.id, since = null))
            unsynced.remove(key)
        } catch(e: Exception) {
            //The data remains to be synced
        }
        syncing.remove(key)
    }
    suspend fun attemptSyncAll() {
        for (item in unsynced.keys) attemptSync(Run(user = item.first, id = item.second ))
    }

    override suspend fun create(run: Run) {
        localRunRepository.create(run)
        if (!isUnsynced(run)) try {
            serverRunRepository.create(run)
        } catch(e: Exception) { unsyncedCreate(run) }
        else attemptSync(run)
    }
    override suspend fun update(runData: RunData) {
        localRunRepository.update(runData)
        val run = runData.run
        if (!isUnsynced(run)) try {
            serverRunRepository.update(runData)
        } catch(e: Exception) { unsyncedUpdate(runData) }
        else attemptSync(run)
    }

    override suspend fun getUpdate(
        user: String,
        id: Long?,
        room: String?,
        event: String?,
        since: Long
    ): RunData {
        if (loginManager.currentUserId() == user) {
            try {
                //throw NotFound("not fohnd")
                return localRunRepository.getUpdate(user, id, room, event, since)
            } catch (e: NotFound) {
                //The run must be from a different device, so synchronize.
                try {
                    val ret = serverRunRepository.getUpdate(user, id, room, event, since)
                    localRunRepository.create(ret.run)
                    localRunRepository.update(ret)
                    return ret
                } catch (e: ServerException) {
                    e.printStackTrace()
                    throw e
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, context.getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show()
                    throw e
                }
            }
        }
        else {
            try {
                return serverRunRepository.getUpdate(user, id, room, event, since)
            } catch (e: ServerException) {
                e.printStackTrace()
                //Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, context.getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show()
                throw e
            }
        }

    }

    override suspend fun getRuns(until: Long, limit: Int): List<RunData> {
        try {
            return serverRunRepository.getRuns(until, limit)
        } catch(e: ServerException) {
            e.printStackTrace()
        }
        catch(e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show()
        }
        return localRunRepository.getRuns(until, limit)
    }

    override suspend fun getRunsSince(since: Long): List<RunData> {
        try {
            return serverRunRepository.getRunsSince(since)
        } catch(e: ServerException) {
            e.printStackTrace()
        }
        catch(e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show()
        }
        return localRunRepository.getRunsSince(since)
    }

    override suspend fun delete(runId: Long) {
        try {
            serverRunRepository.delete(runId)
        } catch(_: Exception) {}
        localRunRepository.delete(runId)
    }

    override suspend fun unfinished(): RunData? {
        var ret: RunData? = null
        try {
            ret = serverRunRepository.unfinished()
        } catch(_: Exception) {}
        if (ret != null) return ret
        return localRunRepository.unfinished()
    }
}