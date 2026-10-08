package com.cherryzp.cherrypokemon.core.database.di

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.cherryzp.cherrypokemon.core.database.CherryPokemonDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun providesCherryPokemonDatabase(
        @ApplicationContext context: Context,
    ): CherryPokemonDatabase = Room.databaseBuilder(
        context,
        CherryPokemonDatabase::class.java,
        "cherry-pokemon-database"
    ).setDriver(AndroidSQLiteDriver())
        .build()
}
