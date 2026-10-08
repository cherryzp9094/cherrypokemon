package com.cherryzp.cherrypokemon.core.database.di

import com.cherryzp.cherrypokemon.core.database.CherryPokemonDatabase
import com.cherryzp.cherrypokemon.core.database.dao.PokemonDao
import com.cherryzp.cherrypokemon.core.database.dao.RemoteKeyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object DaosModule {
    @Provides
    fun providesPokemonDao(database: CherryPokemonDatabase): PokemonDao = database.pokemonDao()

    @Provides
    fun providesRemoteKeyDao(database: CherryPokemonDatabase): RemoteKeyDao =
        database.remoteKeyDao()
}
