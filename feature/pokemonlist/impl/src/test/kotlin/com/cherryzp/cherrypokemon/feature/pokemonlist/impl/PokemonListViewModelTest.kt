package com.cherryzp.cherrypokemon.feature.pokemonlist.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.testing.asSnapshot
import com.cherryzp.cherrypokemon.core.model.AppLanguage
import com.cherryzp.cherrypokemon.core.model.Pokemon
import com.cherryzp.cherrypokemon.core.model.PokemonFilter
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.testing.repository.FakePokemonRepository
import com.cherryzp.cherrypokemon.core.testing.repository.FakeUserDataRepository
import com.cherryzp.cherrypokemon.core.testing.util.MainDispatcherRule
import java.io.IOException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.orbitmvi.orbit.test.test

class PokemonListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val pokemonRepository = FakePokemonRepository()
    private val userDataRepository = FakeUserDataRepository()

    @Test
    fun pokemons_repositoryHasData_emitsThemInOrder() = runTest {
        pokemonRepository.sendPokemons(
            listOf(
                Pokemon(id = 1, name = "bulbasaur", imageUrl = "https://example.com/1.png"),
                Pokemon(id = 4, name = "charmander", imageUrl = "https://example.com/4.png")
            )
        )
        val viewModel =
            PokemonListViewModel(pokemonRepository, userDataRepository, SavedStateHandle())

        val items = viewModel.pokemons.asSnapshot()

        assertEquals(listOf("bulbasaur", "charmander"), items.map { it.name })
        // cachedIn 이 띄운 코루틴을 정리하지 않으면 runTest 가 끝나지 않는다.
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun setLanguage_savesAndShowsChosenLanguage() = runTest {
        val viewModel =
            PokemonListViewModel(pokemonRepository, userDataRepository, SavedStateHandle())

        viewModel.test(this) {
            runOnCreate()
            expectInitialState()

            viewModel.setLanguage(AppLanguage.KOREAN)

            expectState { copy(language = AppLanguage.KOREAN) }
            cancelAndIgnoreRemainingItems()
        }
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun setQuery_showsQueryAndFillsIndex() = runTest {
        val viewModel =
            PokemonListViewModel(pokemonRepository, userDataRepository, SavedStateHandle())

        viewModel.test(this) {
            runOnCreate()
            expectInitialState()

            viewModel.setQuery("pika")

            expectState { copy(query = "pika") }
            // 색인은 상태와 따로 받는다. 끝나기를 기다린다.
            testScheduler.runCurrent()
            cancelAndIgnoreRemainingItems()
        }
        // 검색은 로컬에서만 하므로 전체 색인이 있어야 한다.
        assertEquals(1, pokemonRepository.indexCount)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun setQuery_severalTimes_fillsIndexOnce() = runTest {
        val viewModel =
            PokemonListViewModel(pokemonRepository, userDataRepository, SavedStateHandle())

        viewModel.test(this) {
            runOnCreate()
            expectInitialState()

            // 글자를 칠 때마다 2000개짜리 색인을 다시 받으면 안 된다.
            viewModel.setQuery("p")
            expectState { copy(query = "p") }
            viewModel.setQuery("pi")
            expectState { copy(query = "pi") }
            viewModel.setQuery("pik")
            expectState { copy(query = "pik") }
            // 색인은 상태와 따로 받는다. 끝나기를 기다린다.
            testScheduler.runCurrent()
            cancelAndIgnoreRemainingItems()
        }
        assertEquals(1, pokemonRepository.indexCount)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun setQuery_indexFails_triesAgainOnNextQuery() = runTest {
        pokemonRepository.indexError = IOException()
        val viewModel =
            PokemonListViewModel(pokemonRepository, userDataRepository, SavedStateHandle())

        viewModel.test(this) {
            runOnCreate()
            expectInitialState()

            viewModel.setQuery("p")
            expectState { copy(query = "p") }
            testScheduler.runCurrent()
            // 오프라인이었다가 돌아오면 다음 검색어에서 받는다.
            pokemonRepository.indexError = null
            viewModel.setQuery("pi")
            expectState { copy(query = "pi") }
            viewModel.setQuery("pik")
            expectState { copy(query = "pik") }
            // 색인은 상태와 따로 받는다. 끝나기를 기다린다.
            testScheduler.runCurrent()
            cancelAndIgnoreRemainingItems()
        }
        assertEquals(2, pokemonRepository.indexCount)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun savedQuery_fillsIndexOnCreate() = runTest {
        // 검색 중에 프로세스가 죽었다 살아나도 색인이 있어야 찾을 수 있다.
        val viewModel = PokemonListViewModel(
            pokemonRepository,
            userDataRepository,
            SavedStateHandle(mapOf("query" to "pika"))
        )

        viewModel.test(this) {
            runOnCreate()
            // 색인은 상태와 따로 받는다. 끝나기를 기다린다.
            testScheduler.runCurrent()
            cancelAndIgnoreRemainingItems()
        }
        assertEquals(1, pokemonRepository.indexCount)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun onCreate_fillsPokemonNames() = runTest {
        val viewModel =
            PokemonListViewModel(pokemonRepository, userDataRepository, SavedStateHandle())

        viewModel.test(this) {
            runOnCreate()
            expectInitialState()
            testScheduler.runCurrent()
            cancelAndIgnoreRemainingItems()
        }
        // 목록 카드가 언어별 이름으로 보이려면 이름을 받아 둬야 한다.
        assertEquals(1, pokemonRepository.namesCount)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun onCreate_fillsFilterData() = runTest {
        val viewModel =
            PokemonListViewModel(pokemonRepository, userDataRepository, SavedStateHandle())

        viewModel.test(this) {
            runOnCreate()
            expectInitialState()
            testScheduler.runCurrent()
            cancelAndIgnoreRemainingItems()
        }
        assertEquals(1, pokemonRepository.filtersCount)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun setTypeFilter_showsItSavesItAndSearchesWithIt() = runTest {
        pokemonRepository.sendPokemons(
            listOf(Pokemon(id = 25, name = "pikachu", imageUrl = "https://example.com/25.png"))
        )
        val savedStateHandle = SavedStateHandle()
        val viewModel =
            PokemonListViewModel(pokemonRepository, userDataRepository, savedStateHandle)

        viewModel.test(this) {
            runOnCreate()
            expectInitialState()
            viewModel.setTypeFilter(PokemonType.Electric)
            expectState { copy(typeFilter = PokemonType.Electric) }
            viewModel.setGenerationFilter(1)
            expectState { copy(generationFilter = 1) }
            testScheduler.runCurrent()
            cancelAndIgnoreRemainingItems()
        }
        viewModel.pokemons.asSnapshot()

        // 검색어가 없어도 거름이 걸리면 로컬 검색으로 간다. 색인도 받는다.
        assertEquals(
            PokemonFilter(type = PokemonType.Electric, generation = 1),
            pokemonRepository.lastFilter
        )
        assertEquals(1, pokemonRepository.indexCount)
        assertEquals("Electric", savedStateHandle.get<String>("type"))
        assertEquals(1, savedStateHandle.get<Int>("generation"))
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun savedFilter_isRestoredIntoState() = runTest {
        val viewModel = PokemonListViewModel(
            pokemonRepository,
            userDataRepository,
            SavedStateHandle(mapOf("type" to "Fire", "generation" to 3))
        )

        val state = viewModel.container.stateFlow.value
        assertEquals(PokemonType.Fire, state.typeFilter)
        assertEquals(3, state.generationFilter)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun setQuery_blank_doesNotFillIndex() = runTest {
        val viewModel =
            PokemonListViewModel(pokemonRepository, userDataRepository, SavedStateHandle())

        viewModel.test(this) {
            runOnCreate()
            expectInitialState()

            viewModel.setQuery("   ")

            expectState { copy(query = "   ") }
            cancelAndIgnoreRemainingItems()
        }
        assertEquals(0, pokemonRepository.indexCount)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun savedQuery_isRestoredIntoState() = runTest {
        // 프로세스가 죽었다 살아나면 시스템이 저장해 둔 값으로 핸들을 채워 준다.
        val viewModel = PokemonListViewModel(
            pokemonRepository,
            userDataRepository,
            SavedStateHandle(mapOf("query" to "pika"))
        )

        assertEquals("pika", viewModel.container.stateFlow.value.query)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun setQuery_writesToSavedStateHandle() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel =
            PokemonListViewModel(pokemonRepository, userDataRepository, savedStateHandle)

        viewModel.test(this) {
            runOnCreate()
            expectInitialState()
            viewModel.setQuery("mew")
            expectState { copy(query = "mew") }
            cancelAndIgnoreRemainingItems()
        }

        assertEquals("mew", savedStateHandle.get<String>("query"))
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun pokemons_repositoryEmpty_emitsNothing() = runTest {
        pokemonRepository.sendPokemons(emptyList())
        val viewModel =
            PokemonListViewModel(pokemonRepository, userDataRepository, SavedStateHandle())

        assertEquals(emptyList<Pokemon>(), viewModel.pokemons.asSnapshot())
        viewModel.viewModelScope.cancel()
    }
}
