package com.cherryzp.cherrypokemon.core.data.repository

import androidx.paging.testing.asSnapshot
import com.cherryzp.cherrypokemon.core.data.FakeDatabaseTransactionRunner
import com.cherryzp.cherrypokemon.core.data.FakeEvolutionChainDao
import com.cherryzp.cherrypokemon.core.data.FakeLocalizedNameDao
import com.cherryzp.cherrypokemon.core.data.FakePokemonDao
import com.cherryzp.cherrypokemon.core.data.FakePokemonNetworkDataSource
import com.cherryzp.cherrypokemon.core.data.FakePokemonNetworkDataSource.Companion.networkPokemon
import com.cherryzp.cherrypokemon.core.data.FakePokemonNetworkDataSource.Companion.networkPokemonDetail
import com.cherryzp.cherrypokemon.core.data.FakePokemonNetworkDataSource.Companion.networkPokemonSpecies
import com.cherryzp.cherrypokemon.core.data.FakeRemoteKeyDao
import com.cherryzp.cherrypokemon.core.database.model.PokemonEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonNameEntity
import com.cherryzp.cherrypokemon.core.model.LocalizedText
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.network.model.NetworkChainLink
import com.cherryzp.cherrypokemon.core.network.model.NetworkEvolutionChain
import com.cherryzp.cherrypokemon.core.network.model.NetworkEvolutionDetail
import com.cherryzp.cherrypokemon.core.network.model.NetworkLanguageName
import com.cherryzp.cherrypokemon.core.network.model.NetworkNamedResource
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonNames
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonPage
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OfflineFirstPokemonRepositoryTest {

    private lateinit var network: FakePokemonNetworkDataSource
    private lateinit var pokemonDao: FakePokemonDao
    private lateinit var localizedNameDao: FakeLocalizedNameDao
    private lateinit var evolutionChainDao: FakeEvolutionChainDao
    private lateinit var remoteKeyDao: FakeRemoteKeyDao
    private lateinit var repository: OfflineFirstPokemonRepository

    @Before
    fun setUp() {
        network = FakePokemonNetworkDataSource()
        localizedNameDao = FakeLocalizedNameDao()
        pokemonDao = FakePokemonDao(names = { localizedNameDao.pokemonNames.value })
        evolutionChainDao = FakeEvolutionChainDao()
        remoteKeyDao = FakeRemoteKeyDao()
        repository = OfflineFirstPokemonRepository(
            pokemonDao = pokemonDao,
            remoteKeyDao = remoteKeyDao,
            localizedNameDao = localizedNameDao,
            evolutionChainDao = evolutionChainDao,
            network = network,
            transaction = FakeDatabaseTransactionRunner()
        )
    }

    @Test
    fun getPokemonDetailStream_emptyLocal_emitsNull() = runTest {
        assertNull(repository.getPokemonDetailStream(pokeId = 1).first())
    }

    @Test
    fun getPokemonDetailStream_otherPokemonSaved_doesNotEmitAgain() = runTest {
        network.detail = networkPokemonDetail(id = 1, name = "bulbasaur")
        network.species = networkPokemonSpecies(id = 1)
        repository.refreshPokemonDetail(pokeId = 1)
        val emitted = collectDetails(pokeId = 1)
        val before = emitted.size

        // 다른 포켓몬을 열어도 같은 테이블에 쓰므로 Room 은 1번 쿼리도 다시 흘린다.
        network.detail =
            networkPokemonDetail(id = 4, name = "charmander", typeNames = listOf("fire"))
        network.species = networkPokemonSpecies(id = 4)
        repository.refreshPokemonDetail(pokeId = 4)
        testScheduler.runCurrent()

        assertEquals(before, emitted.size)
    }

    @Test
    fun getPokemonDetailStream_namesAdded_doesNotReopenChainQuery() = runTest {
        network.detail = networkPokemonDetail(id = 1, name = "bulbasaur")
        network.species = networkPokemonSpecies(id = 1)
        repository.refreshPokemonDetail(pokeId = 1)
        collectDetails(pokeId = 1)
        val before = evolutionChainDao.chainQueryCount

        // 다른 포켓몬을 열면서 새 타입·특성 이름이 저장된다.
        network.detail =
            networkPokemonDetail(id = 4, name = "charmander", typeNames = listOf("fire"))
        network.species = networkPokemonSpecies(id = 4)
        repository.refreshPokemonDetail(pokeId = 4)
        testScheduler.runCurrent()

        assertEquals(before, evolutionChainDao.chainQueryCount)
    }

    /** 스트림이 흘린 값을 모두 모은다. 테스트가 끝나면 수집도 끝난다. */
    private fun TestScope.collectDetails(pokeId: Int): List<PokemonDetail?> {
        val emitted = mutableListOf<PokemonDetail?>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.getPokemonDetailStream(pokeId).toList(emitted)
        }
        return emitted
    }

    @Test
    fun searchPokemonsStream_findsByNameAndNumber() = runTest {
        pokemonDao.insertPokemons(
            listOf(
                PokemonEntity(id = 25, name = "pikachu", imageUrl = ""),
                PokemonEntity(id = 150, name = "mewtwo", imageUrl = "")
            )
        )

        assertEquals(
            listOf("pikachu"),
            repository.searchPokemonsStream("pika").asSnapshot().map { it.name }
        )
        assertEquals(
            listOf("mewtwo"),
            repository.searchPokemonsStream("150").asSnapshot().map { it.name }
        )
    }

    @Test
    fun ensurePokemonNames_savesOnce() = runTest {
        network.pokemonNames = NetworkPokemonNames(
            speciesNames = listOf(
                NetworkLanguageName("피카츄", speciesId = 25, languageId = 3),
                NetworkLanguageName("Pikachu", speciesId = 25, languageId = 9)
            )
        )

        repository.ensurePokemonNames()
        repository.ensurePokemonNames()

        // 이미 받아 두었으면 다시 받지 않는다.
        assertEquals(1, network.namesCallCount)
        assertEquals("피카츄", localizedNameDao.pokemonNames.value[25]?.korean)
    }

    @Test
    fun pokemonsStream_attachesSavedNames() = runTest {
        pokemonDao.insertPokemons(listOf(PokemonEntity(id = 25, name = "pikachu", imageUrl = "")))
        localizedNameDao.insertPokemonNames(
            listOf(PokemonNameEntity(id = 25, korean = "피카츄", english = "Pikachu"))
        )

        val pokemon = repository.getPokemonsStream().asSnapshot().single()

        assertEquals(LocalizedText(korean = "피카츄", english = "Pikachu"), pokemon.localizedName)
    }

    @Test
    fun searchPokemonsStream_findsByKoreanName() = runTest {
        pokemonDao.insertPokemons(
            listOf(
                PokemonEntity(id = 25, name = "pikachu", imageUrl = ""),
                PokemonEntity(id = 133, name = "eevee", imageUrl = "")
            )
        )
        localizedNameDao.insertPokemonNames(
            listOf(PokemonNameEntity(id = 25, korean = "피카츄", english = "Pikachu"))
        )

        assertEquals(
            listOf("pikachu"),
            repository.searchPokemonsStream("피카").asSnapshot().map { it.name }
        )
    }

    @Test
    fun ensurePokemonIndex_savesEveryName() = runTest {
        network.page = NetworkPokemonPage(
            count = 2,
            results = listOf(
                networkPokemon(id = 25, name = "pikachu"),
                networkPokemon(id = 150, name = "mewtwo")
            )
        )

        repository.ensurePokemonIndex()

        assertEquals(listOf("pikachu", "mewtwo"), pokemonDao.pokemons.map { it.name })
        // 전부 받았으므로 목록 끝에서 더 받으러 가지 않는다.
        assertNull(remoteKeyDao.getRemoteKey()?.nextOffset)
        assertNotNull(remoteKeyDao.getRemoteKey())
    }

    @Test
    fun ensurePokemonIndex_alreadyComplete_doesNotWriteAgain() = runTest {
        network.page = NetworkPokemonPage(
            count = 1,
            results = listOf(networkPokemon(id = 25, name = "pikachu"))
        )
        repository.ensurePokemonIndex()

        network.page = NetworkPokemonPage(count = 1, results = emptyList())
        repository.ensurePokemonIndex()

        // 색인이 다 차 있으면 응답이 비어도 지우지 않는다.
        assertEquals(1, pokemonDao.pokemons.size)
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
    fun refreshPokemonDetail_savesEvolutionChain() = runTest {
        network.detail = networkPokemonDetail(id = 1, name = "bulbasaur")
        network.species = networkPokemonSpecies(id = 1)

        repository.refreshPokemonDetail(pokeId = 1)

        val stages = repository.getPokemonDetailStream(pokeId = 1).first()?.evolutionStages
        assertEquals(listOf(1, 2), stages?.map { it.pokemonId })
        assertEquals(listOf(1, 2), stages?.map { it.stage })
        assertEquals(16, stages?.last()?.condition?.minLevel)
    }

    @Test
    fun refreshPokemonDetail_chainWithItem_savesItemNameOnce() = runTest {
        network.detail = networkPokemonDetail(id = 25, name = "pikachu")
        network.species = networkPokemonSpecies(id = 25)
        network.evolutionChain = NetworkEvolutionChain(
            id = 1,
            chain = NetworkChainLink(
                species = NetworkNamedResource(
                    "pikachu",
                    "https://pokeapi.co/api/v2/pokemon-species/25/"
                ),
                evolvesTo = listOf(
                    NetworkChainLink(
                        species = NetworkNamedResource(
                            "raichu",
                            "https://pokeapi.co/api/v2/pokemon-species/26/"
                        ),
                        evolutionDetails = listOf(
                            NetworkEvolutionDetail(item = NetworkNamedResource("thunder-stone"))
                        )
                    )
                )
            )
        )

        repository.refreshPokemonDetail(pokeId = 25)
        repository.refreshPokemonDetail(pokeId = 25)

        assertEquals("천둥의돌", localizedNameDao.savedItemNames["thunder-stone"]?.korean)
        // 받아 둔 도구 이름은 다시 받지 않는다.
        assertEquals(1, network.itemCallCount)
    }

    @Test
    fun refreshPokemonDetail_chainAlreadySaved_doesNotCallAgain() = runTest {
        network.detail = networkPokemonDetail(id = 1, name = "bulbasaur")
        network.species = networkPokemonSpecies(id = 1)
        repository.refreshPokemonDetail(pokeId = 1)

        repository.refreshPokemonDetail(pokeId = 1)

        // 같은 체인의 포켓몬을 열어도 체인은 한 번만 받는다.
        assertEquals(1, network.chainCallCount)
    }

    @Test
    fun refreshPokemonDetail_firstOpen_callsNetworkInParallel() = runTest {
        network.detail = networkPokemonDetail(id = 1, name = "bulbasaur")
        network.species = networkPokemonSpecies(id = 1)
        network.callDelayMillis = 100

        repository.refreshPokemonDetail(pokeId = 1)

        // 상세·종(100) 다음에 자기 타입 → 약점·저항 타입(200)이 가장 길다. 특성 둘과 체인은
        // 그동안 함께 받는다. 하나씩 기다리면 800 이 걸린다.
        assertEquals(300, testScheduler.currentTime)
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
