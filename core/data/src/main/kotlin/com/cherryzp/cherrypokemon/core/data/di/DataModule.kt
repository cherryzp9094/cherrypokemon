package com.cherryzp.cherrypokemon.core.data.di

import com.cherryzp.cherrypokemon.core.data.repository.DefaultUserDataRepository
import com.cherryzp.cherrypokemon.core.data.repository.OfflineFirstPokemonRepository
import com.cherryzp.cherrypokemon.core.data.repository.PokemonRepository
import com.cherryzp.cherrypokemon.core.data.repository.UserDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** 테스트에서 @TestInstallIn 으로 교체하므로 public 이다. */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    internal abstract fun bindsPokemonRepository(
        repository: OfflineFirstPokemonRepository,
    ): PokemonRepository

    @Binds
    internal abstract fun bindsUserDataRepository(
        repository: DefaultUserDataRepository,
    ): UserDataRepository
}
