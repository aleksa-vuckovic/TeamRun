package com.example.runpal.repositories.run

import com.example.runpal.ServerException
import com.example.runpal.models.Run
import com.example.runpal.models.RunData
import com.example.runpal.server.RunApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerRunRepository @Inject constructor(private val runApi: RunApi): RunRepository {
    override suspend fun create(run: Run) {
        val ret = runApi.create(run)
        if (ret.message != "ok") throw ServerException(ret.message)
    }

    override suspend fun update(runData: RunData) {
        val ret = runApi.update(runData)
        if (ret.message != "ok") throw ServerException(ret.message)
    }

    override suspend fun getUpdate(
        user: String,
        id: Long?,
        room: String?,
        event: String?,
        since: Long
    ): RunData {
        val ret = runApi.getUpdate(user = user, id = id, room = room, event = event, since = since)
        if (ret.message != "ok") throw ServerException(ret.message)
        else return ret.data!!
    }

    override suspend fun getRuns(until: Long, limit: Int): List<RunData> {
        val response = runApi.getAll(until = until, limit = limit)
        if (response.message != "ok") throw ServerException(response.message)
        else return response.data!!
    }

    override suspend fun getRunsSince(since: Long): List<RunData> {
        val response = runApi.getSince(since)
        if (response.message != "ok") throw ServerException(response.message)
        else return response.data!!
    }

    override suspend fun unfinished(): RunData? {
        val response = runApi.unfinished()
        if (response.message != "ok") throw ServerException(response.message)
        else return response.data!!
    }

    override suspend fun delete(runId: Long) {
        val response = runApi.delete(runId)
        if (response.message != "ok") throw ServerException(response.message)
    }
}