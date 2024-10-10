package com.example.runpal.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface SyncDao {

    @Insert
    suspend fun add(sync: Sync)

    @Query("select * from sync")
    suspend fun getAll(): List<Sync>

    @Query("select * from sync where user = :user and runId = :runId")
    suspend fun get(user: String, runId: Long): Sync?

    @Delete
    suspend fun delete(sync: Sync)

    @Insert
    suspend fun insert(sync: Sync)

    @Update
    suspend fun update(sync: Sync)

    @Query("delete from sync")
    suspend fun deleteAll()
}