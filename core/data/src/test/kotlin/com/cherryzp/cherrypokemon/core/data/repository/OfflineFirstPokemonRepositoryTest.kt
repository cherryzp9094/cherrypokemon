package com.cherryzp.cherrypokemon.core.data.repository

import com.cherryzp.cherrypokemon.core.data.FakeDatabaseTransactionRunner
import com.cherryzp.cherrypokemon.core.data.FakeLocalizedNameDao
import com.cherryzp.cherrypokemon.core.data.FakePokemonDao
import com.cherryzp.cherrypokemon.core.data.FakePokemonNetworkDataSource
import com.cherryzp.cherrypokemon.core.data.FakePokemonNetworkDataSource.Companion.networkPokemonDetail
import com.cherryzp.cherrypokemon.core.data.FakePokemonNetworkDataSource.Companion.networkPokemonSpecies
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
    private lateinit var localizedNameDao: FakeLocalizedNameDao
    private lateinit var repository: OfflineFirstPokemonRepository

    @Before
    fun setUp() {
        network = FakePokemonNetworkDataSource()
        pokemonDao = FakePokemonDao()
        localizedNameDao = FakeLocalizedNameDao()
        repository = OfflineFirstPokemonRepository(
            pokemonDao = pokemonDao,
            remoteKeyDao = FakeRemoteKeyDao(),
            localizedNameDao = localizedNameDao,
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
        network.species = networkPokemonSpecies(id = 1)

        repository.refreshPokemonDetail(pokeId = 1)

        val detail = repository.getPokemonDetailStream(pokeId = 1).first()
        assertEquals("bulbasaur", detail?.name)
        assertEquals("이상해씨", detail?.localizedName?.korean)
        assertEquals("Bulbasaur", detail?.localizedName?.english)
        assertEquals("씨앗포켓몬", detail?.genus?.korean)
        assertEquals(0.7f, detail?.heightMeters)
        assertEquals(
            listOf(PokemonType.Grass, PokemonType.Poison),
            detail?.types?.map { it.type }
        )
    }

    @Test
    fun refreshPokemonDetail_savesTypeAndAbilityNames() = runTest {
        network.detail = networkPokemonDetail(id = 1, name = "bulbasaur")
        network.species = networkPokemonSpecies(id = 1)

        repository.refreshPokemonDetail(pokeId = 1)

        val detail = repository.getPokemonDetailStream(pokeId = 1).first()
        assertEquals("풀", detail?.types?.first()?.name?.korean)
        assertEquals("심록", detail?.abilities?.first()?.localizedName?.korean)
    }

    @Test
    fun refreshPokemonDetail_alreadySavedNames_doesNotCallAgain() = runTest {
        network.detail = networkPokemonDetail(id = 1, name = "bulbasaur")
        network.species = networkPokemonSpecies(id = 1)
        repository.refreshPokemonDetail(pokeId = 1)
        val firstCalls = network.nameCallCount

        repository.refreshPokemonDetail(pokeId = 1)

        // 받아 둔 이름은 다시 받지 않는다.
        assertEquals(firstCalls, network.nameCallCount)
        assertTrue(firstCalls > 0)
    }

    @Test
    fun refreshPokemonDetail_speciesFails_throwsAndWritesNothing() = runTest {
        network.detail = networkPokemonDetail(id = 1, name = "bulbasaur")
        network.speciesError = IOException()

        val thrown = runCatching { repository.refreshPokemonDetail(pokeId = 1) }.exceptionOrNull()

        assertTrue(thrown is IOException)
        // 둘 중 하나만 실패해도 반쪽짜리 상세를 남기지 않는다.
        assertNull(repository.getPokemonDetailStream(pokeId = 1).first())
    }

    @Test
    fun refreshPokemonDetail_networkError_throwsAndKeepsLocalData() = runTest {
        network.detail = networkPokemonDetail(id = 1, name = "bulbasaur")
        network.species = networkPokemonSpecies(id = 1)
        repository.refreshPokemonDetail(pokeId = 1)
        network.error = IOException()

        val thrown = runCatching { repository.refreshPokemonDetail(pokeId = 1) }.exceptionOrNull()

        assertTrue(thrown is IOException)

        assertEquals("bulbasaur", repository.getPokemonDetailStream(pokeId = 1).first()?.name)
    }
}
