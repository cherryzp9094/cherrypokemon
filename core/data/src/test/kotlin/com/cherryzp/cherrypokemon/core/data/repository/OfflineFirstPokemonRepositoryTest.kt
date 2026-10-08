package com.cherryzp.cherrypokemon.core.data.repository

import com.cherryzp.cherrypokemon.core.data.FakeDatabaseTransactionRunner
import com.cherryzp.cherrypokemon.core.data.FakePokemonDao
import com.cherryzp.cherrypokemon.core.data.FakePokemonNetworkDataSource
import com.cherryzp.cherrypokemon.core.data.FakePokemonNetworkDataSource.Companion.networkPokemonDetail
import com.cherryzp.cherrypokemon.core.data.FakeRemoteKeyDao
import com.cherryzp.cherrypokemon.core.model.PokemonType
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OfflineFirstPokemonRepositoryTest {

    private lateinit var network: FakePokemonNetworkDataSource
    private lateinit var pokemonDao: FakePokemonDao
    private lateinit var repository: OfflineFirstPokemonRepository

    @Before
    fun setUp() {
        network = FakePokemonNetworkDataSource()
        pokemonDao = FakePokemonDao()
        repository = OfflineFirstPokemonRepository(
            pokemonDao = pokemonDao,
            remoteKeyDao = FakeRemoteKeyDao(),
            network = network,
            transaction = FakeDatabaseTransactionRunner()
        )
    }

    @Test
    fun getPokemonDetailStream_emptyLocal_emitsNull() = runTest {
        assertNull(repository.getPokemonDetailStream(pokeId = 1).first())
    }

    @Test
    fun refreshPokemonDetail_savesToLocalAndStreamEmitsExternalModel() = runTest {
        network.detail = networkPokemonDetail(
            id = 1,
            name = "bulbasaur",
            typeNames = listOf("grass", "poison")
        )

        repository.refreshPokemonDetail(pokeId = 1)

        val detail = repository.getPokemonDetailStream(pokeId = 1).first()
        assertEquals("bulbasaur", detail?.name)
        assertEquals(0.7f, detail?.heightMeters)
        assertEquals(listOf(PokemonType.Grass, PokemonType.Poison), detail?.types)
    }

    @Test
    fun refreshPokemonDetail_networkError_throwsAndKeepsLocalData() = runTest {
        network.detail = networkPokemonDetail(id = 1, name = "bulbasaur")
        repository.refreshPokemonDetail(pokeId = 1)
        network.error = IOException()

        val thrown = runCatching { repository.refreshPokemonDetail(pokeId = 1) }.exceptionOrNull()

        assertTrue(thrown is IOException)

        assertEquals("bulbasaur", repository.getPokemonDetailStream(pokeId = 1).first()?.name)
    }
}
