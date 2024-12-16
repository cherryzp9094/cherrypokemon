package com.cherryzp.data.di

import com.cherryzp.data.api.PokemonApi
import com.cherryzp.data.repository.PokemonRepositoryImpl
import com.cherryzp.domain.repository.PokemonRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    fun bindsPokemonRepository(
        pokemonRepositoryImpl: PokemonRepositoryImpl
    ): PokemonRepository
}