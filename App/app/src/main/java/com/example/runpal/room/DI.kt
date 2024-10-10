package com.example.runpal.room

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): Database {
        return Room.databaseBuilder(context, Database::class.java, "RunDatabase").fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideRunDao(database: Database): RunDao = database.runDao()

    @Provides
    @Singleton
    fun providePathDao(database: Database): PathDao = database.pathDao()

    @Provides
    @Singleton
    fun provideUserDao(database: Database): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideSyncDao(database: Database): SyncDao = database.syncDao()

}
