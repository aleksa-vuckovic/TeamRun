package com.example.runpal.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PathDao {

    @Insert
    suspend fun insert(pathPoint: Path)


    @Query("select * from path where user = :user and runId = :runId and time > :since")
    suspend fun get(user: String, runId: Long, since: Long = 0): List<Path>
    //No need to 'order by' since the entries will be naturally ordered by insertion time??

    @Query("select * from path where user = :user and runId = :runId order by time desc")
    suspend fun last(user: String, runId: Long): Path?

    @Query("delete from path")
    suspend fun deleteAll()

    @Query("delete from path where user = :user and runId = :runId")
    suspend fun deleteAllForRun(user: String, runId: Long)

}