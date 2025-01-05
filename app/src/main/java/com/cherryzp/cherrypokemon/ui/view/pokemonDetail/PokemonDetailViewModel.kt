package com.cherryzp.cherrypokemon.ui.view.pokemonDetail

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.cherryzp.cherrypokemon.ui.view.base.base.BaseViewModel
import com.cherryzp.consts.KeyConsts.POKEMON_BACKGROUND_COLOR
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
        get() = PokemonDetailUiState(
            pokemonBackgroundColor = savedStateHandle.get<Int>(POKEMON_BACKGROUND_COLOR) ?: Color.White.value.toInt()
        )

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