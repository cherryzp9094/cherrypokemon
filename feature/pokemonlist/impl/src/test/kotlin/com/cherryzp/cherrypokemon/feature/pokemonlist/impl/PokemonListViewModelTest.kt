package com.cherryzp.cherrypokemon.feature.pokemonlist.impl

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
        val viewModel = PokemonListViewModel(pokemonRepository, userDataRepository)

        val items = viewModel.pokemons.asSnapshot()

        assertEquals(listOf("bulbasaur", "charmander"), items.map { it.name })
        // cachedIn 이 띄운 코루틴을 정리하지 않으면 runTest 가 끝나지 않는다.
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun setLanguage_savesAndShowsChosenLanguage() = runTest {
        val viewModel = PokemonListViewModel(pokemonRepository, userDataRepository)

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
    fun pokemons_repositoryEmpty_emitsNothing() = runTest {
        pokemonRepository.sendPokemons(emptyList())
        val viewModel = PokemonListViewModel(pokemonRepository, userDataRepository)

        assertEquals(emptyList<Pokemon>(), viewModel.pokemons.asSnapshot())
        viewModel.viewModelScope.cancel()
    }
}
