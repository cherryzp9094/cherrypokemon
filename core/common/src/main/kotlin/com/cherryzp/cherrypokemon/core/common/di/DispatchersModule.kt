package com.cherryzp.cherrypokemon.core.common.di

import com.cherryzp.cherrypokemon.core.common.network.CherryPokemonDispatchers
import com.cherryzp.cherrypokemon.core.common.network.Dispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** 테스트에서 @TestInstallIn 으로 교체하므로 public 이다. */
@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {
    @Provides
    @Dispatcher(CherryPokemonDispatchers.IO)
    fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(CherryPokemonDispatchers.Default)
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
