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

/**
 * 목록 화면의 ViewModel.
 *
 * 화면 상태가 Paging 쪽에 있어서 UI 상태 타입이 따로 없다.
 * `PagingData` 는 UI 상태에 넣지 않고 별도 스트림으로 노출한다.
 */
@HiltViewModel
class PokemonListViewModel @Inject constructor(pokemonRepository: PokemonRepository) :
    ViewModel() {

    val pokemons: Flow<PagingData<Pokemon>> =
        pokemonRepository.getPokemonsStream().cachedIn(viewModelScope)
}
