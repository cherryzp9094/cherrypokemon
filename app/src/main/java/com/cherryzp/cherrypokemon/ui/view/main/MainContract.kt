package com.cherryzp.cherrypokemon.ui.view.main

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.paging.PagingData
import com.cherryzp.cherrypokemon.ui.view.base.base.UiSideEffect
import com.cherryzp.cherrypokemon.ui.view.base.base.UiState
import com.cherryzp.domain.model.Pokemon
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.Flow

@Stable
data class MainUiState(
    val pokemons: Flow<PagingData<Pokemon>>? = null,
    val pokemonBackgroundColor: ImmutableMap<Int, Color> = persistentMapOf()
): UiState()

sealed class MainUiSideEffect: UiSideEffect {
    data class goPokemonDetail(
        val pokeId: Int,
        val pokemonBackgroundColor: Int
    ): MainUiSideEffect()
}