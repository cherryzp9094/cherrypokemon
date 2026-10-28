package com.cherryzp.cherrypokemon.feature.pokemonlist.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.testing.asSnapshot
import com.cherryzp.cherrypokemon.core.model.AppLanguage
import com.cherryzp.cherrypokemon.core.model.Pokemon
import com.cherryzp.cherrypokemon.core.testing.repository.FakePokemonRepository
import com.cherryzp.cherrypokemon.core.testing.repository.FakeUserDataRepository
import com.cherryzp.cherrypokemon.core.testing.util.MainDispatcherRule
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
            cancelAndIgnoreRemainingItems()
        }
        // 검색은 로컬에서만 하므로 전체 색인이 있어야 한다.
        assertEquals(1, pokemonRepository.indexCount)
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
