package com.cherryzp.cherrypokemon.core.database.di

import com.cherryzp.cherrypokemon.core.database.CherryPokemonDatabase
import com.cherryzp.cherrypokemon.core.database.dao.EvolutionChainDao
import com.cherryzp.cherrypokemon.core.database.dao.LocalizedNameDao
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

    @Provides
    fun providesLocalizedNameDao(database: CherryPokemonDatabase): LocalizedNameDao =
        database.localizedNameDao()

    @Provides
    fun providesEvolutionChainDao(database: CherryPokemonDatabase): EvolutionChainDao =
        database.evolutionChainDao()
}
