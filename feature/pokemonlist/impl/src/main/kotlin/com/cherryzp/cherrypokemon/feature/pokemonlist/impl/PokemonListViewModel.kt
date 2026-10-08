package com.cherryzp.cherrypokemon.feature.pokemonlist.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.cherryzp.cherrypokemon.core.data.repository.PokemonRepository
import com.cherryzp.cherrypokemon.core.model.Pokemon
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

/**
 * 목록 화면 ViewModel.
 *
 * 목록 자체는 Paging 이 들고 있어서 UI 상태에 담지 않는다. 상태 컨테이너는
 * 다른 화면과 같은 형태를 쓰려고 두었고, 지금은 담는 값이 없다.
 */
@HiltViewModel
class PokemonListViewModel @Inject constructor(pokemonRepository: PokemonRepository) :
    ViewModel(),
    OrbitContainerHost<PokemonListUiState, PokemonListUiState, PokemonListSideEffect> {

    override val container =
        orbitContainer<PokemonListUiState, PokemonListSideEffect>(PokemonListUiState)

    val pokemons: Flow<PagingData<Pokemon>> =
        pokemonRepository.getPokemonsStream().cachedIn(viewModelScope)
}
