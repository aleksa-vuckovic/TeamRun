package com.example.runpal.repositories

import com.example.runpal.repositories.run.CombinedRunRepository
import com.example.runpal.repositories.run.RunRepository
import com.example.runpal.repositories.user.CombinedUserRepository
import com.example.runpal.repositories.user.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {

    @Binds
    abstract fun bindUserRepository(combinedUserRepository: CombinedUserRepository): UserRepository

    @Binds
    abstract fun bindRunRepository(combinedRunRepository: CombinedRunRepository): RunRepository
}