package com.cherryzp.cherrypokemon.core.database.di

import com.cherryzp.cherrypokemon.core.database.DatabaseTransactionRunner
import com.cherryzp.cherrypokemon.core.database.RoomDatabaseTransactionRunner
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TransactionModule {
    @Binds
    abstract fun bindsDatabaseTransactionRunner(
        runner: RoomDatabaseTransactionRunner,
    ): DatabaseTransactionRunner
}
