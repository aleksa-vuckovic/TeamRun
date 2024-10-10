package com.example.runpal.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.runpal.models.Run


@Dao
interface RunDao {

    @Query("select * from runs where user = :user and id = :id")
    suspend fun findById(user: String, id: Long): Run?
    @Query("select * from runs where user = :user and room = :room")
    suspend fun findByRoom(user: String, room: String): Run?
    @Query("select * from runs where user = :user and event = :event")
    suspend fun findByEvent(user: String, event: String): Run


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(run: Run)

    @Update
    suspend fun update(run: Run)

    @Query("select * from runs where user = :user and  `end` is not null and  start < :until order by start desc limit :limit")
    suspend fun all(user: String, until: Long, limit: Int): List<Run>


    @Query("select * from runs where user = :user and `end` is not null and start >= :since order by start asc")
    suspend fun allSince(user: String, since: Long): List<Run>

    @Query("select * from runs where user = :user and `end` is null order by start desc")
    suspend fun unfinished(user: String): Run?

    @Query("delete from runs")
    suspend fun deleteAll()

    @Query("delete from runs where user = :user and id = :runId")
    suspend fun delete(user: String, runId: Long)

}