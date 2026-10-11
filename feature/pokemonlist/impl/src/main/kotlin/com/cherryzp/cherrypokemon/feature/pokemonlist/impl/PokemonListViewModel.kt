package com.cherryzp.cherrypokemon.feature.pokemonlist.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.cherryzp.cherrypokemon.core.data.repository.PokemonRepository
import com.cherryzp.cherrypokemon.core.data.repository.UserDataRepository
import com.cherryzp.cherrypokemon.core.model.AppLanguage
import com.cherryzp.cherrypokemon.core.model.Pokemon
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    pokemonRepository: PokemonRepository,
    private val userDataRepository: UserDataRepository,
) : ViewModel(),
    OrbitContainerHost<PokemonListUiState, PokemonListUiState, PokemonListSideEffect> {

    override val container = orbitContainer<PokemonListUiState, PokemonListSideEffect>(
        initialState = PokemonListUiState()
    ) {
        repeatOnSubscription {
            userDataRepository.userData.collect { userData ->
                val language = userData.language ?: return@collect
                reduce { state.copy(language = language) }
            }
        }
    }

    val pokemons: Flow<PagingData<Pokemon>> =
        pokemonRepository.getPokemonsStream().cachedIn(viewModelScope)

    fun setLanguage(language: AppLanguage) = intent {
        userDataRepository.setLanguage(language)
    }
}
