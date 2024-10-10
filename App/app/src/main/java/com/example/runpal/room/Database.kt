package com.example.runpal.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.runpal.models.Run
import com.example.runpal.models.User


@TypeConverters(BitmapConverter::class)
@Database(entities = [User::class, Run::class, Path::class, Sync::class], version = 4, exportSchema = false)
abstract class Database: RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun runDao(): RunDao
    abstract fun pathDao(): PathDao
    abstract fun syncDao(): SyncDao
}