package com.cherryzp.cherrypokemon.core.data.test

import com.cherryzp.cherrypokemon.core.data.di.DataModule
import com.cherryzp.cherrypokemon.core.data.repository.PokemonRepository
import com.cherryzp.cherrypokemon.core.data.repository.UserDataRepository
import com.cherryzp.cherrypokemon.core.testing.repository.FakePokemonRepository
import com.cherryzp.cherrypokemon.core.testing.repository.FakeUserDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

/** Hilt 계측 테스트에서 Repository 를 Fake 로 바꾼다. */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DataModule::class])
internal abstract class TestDataModule {
    @Binds
    abstract fun bindsPokemonRepository(repository: FakePokemonRepository): PokemonRepository

    @Binds
    abstract fun bindsUserDataRepository(repository: FakeUserDataRepository): UserDataRepository
}
