package com.example.runpal.repositories.run

import com.example.runpal.NotFound
import com.example.runpal.models.PathPoint
import com.example.runpal.models.Run
import com.example.runpal.models.RunData
import com.example.runpal.repositories.LoginManager
import com.example.runpal.room.PathDao
import com.example.runpal.room.RunDao
import com.example.runpal.room.toPath
import com.example.runpal.room.toPathPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRunRepository @Inject constructor (
    private val runDao: RunDao,
    private val pathDao: PathDao,
    private val loginManager: LoginManager
):
    RunRepository {
    override suspend fun create(run: Run) {
        runDao.insert(run)
        pathDao.insert(PathPoint.INIT.toPath(run.user, run.id))
    }

    override suspend fun update(runData: RunData) {
        //location is ignored as it is not saved locally and does not need to be
        runDao.update(runData.run)
        for (pathPoint in runData.path) {
            pathDao.insert(pathPoint.toPath(runData.run.user, runData.run.id))
        }
    }

    override suspend fun getUpdate(
        user: String,
        id: Long?,
        room: String?,
        event: String?,
        since: Long
    ): RunData {
        val run: Run?
        if (id != null) run = runDao.findById(user, id)
        else if (room != null) run = runDao.findByRoom(user, room)
        else if (event != null) run = runDao.findByEvent(user, event)
        else throw NotFound("Not enough data to identify run.")
        if (run == null) throw NotFound("Run not found in local database.")

        val path = pathDao.get(run.user, run.id, since).map { it.toPathPoint() }
        val ret = RunData(run = run, path = path, location = path.lastOrNull() ?: PathPoint.INIT)
        return ret
    }

    override suspend fun getRuns(until: Long, limit: Int): List<RunData> {
        val user = loginManager.currentUserId()!!
        val runs = runDao.all(user, until, limit)
        val ret = mutableListOf<RunData>()
        for (run in runs) {
            val loc = pathDao.last(user, run.id)
            if (loc == null) ret.add(RunData(run = run, location = PathPoint.INIT))
            else ret.add(RunData(run = run, location = loc.toPathPoint()))
        }
        return ret
    }

    override suspend fun getRunsSince(since: Long): List<RunData> {
        val user = loginManager.currentUserId()!!
        val runs = runDao.allSince(user, since)
        val ret = mutableListOf<RunData>()
        for (run in runs) {
            val loc = pathDao.last(user, run.id)
            if (loc == null) ret.add(RunData(run = run, location = PathPoint.INIT))
            else ret.add(RunData(run = run, location = loc.toPathPoint()))
        }
        return ret
    }

    override suspend fun unfinished(): RunData? {
        val user = loginManager.currentUserId()!!
        val run = runDao.unfinished(user) ?: return null
        val loc = pathDao.last(user, run.id)
        return RunData(run = run, location = loc!!.toPathPoint())
    }

    override suspend fun delete(runId: Long) {
        runDao.delete(loginManager.currentUserId()!!, runId)
    }
}