package com.cherryzp.cherrypokemon.ui.view.main

import androidx.compose.runtime.Immutable
import androidx.paging.PagingData
import com.cherryzp.cherrypokemon.ui.view.base.base.UiSideEffect
import com.cherryzp.cherrypokemon.ui.view.base.base.UiState
import com.cherryzp.domain.model.Pokemon
import kotlinx.coroutines.flow.Flow

@Immutable
data class MainUiState(
    val pokemons: Flow<PagingData<Pokemon>>? = null,
): UiState()

sealed class MainUiSideEffect: UiSideEffect