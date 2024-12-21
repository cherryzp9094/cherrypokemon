package com.cherryzp.cherrypokemon.ui.view.pokemonDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.cherryzp.cherrypokemon.ui.view.base.base.BaseViewModel
import com.cherryzp.consts.KeyConsts.POKE_NO
import com.cherryzp.data.extend.default
import com.cherryzp.domain.usecase.PokemonDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val pokemonDetailUseCase: PokemonDetailUseCase
): BaseViewModel<PokemonDetailUiState>() {
    override val initialState: PokemonDetailUiState
        get() = PokemonDetailUiState()

    init {
        fetchData()
    }

    fun fetchData() = viewModelScope.launch {
        val pokemonDetail = pokemonDetailUseCase(
            savedStateHandle.get<Int>(POKE_NO).default()
        ).single()

        reduceState {
            it.copy(
                pokemonDetail = pokemonDetail
            )
        }
    }
}