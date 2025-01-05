package com.cherryzp.cherrypokemon.ui.view.pokemonDetail

import com.cherryzp.cherrypokemon.ui.view.base.base.UiSideEffect
import com.cherryzp.cherrypokemon.ui.view.base.base.UiState
import com.cherryzp.domain.model.PokemonDetail

data class PokemonDetailUiState(
    val pokemonDetail: PokemonDetail? = null,
    val pokemonBackgroundColor: Int
): UiState()

sealed class PokemonDetailUiSideEffect: UiSideEffect