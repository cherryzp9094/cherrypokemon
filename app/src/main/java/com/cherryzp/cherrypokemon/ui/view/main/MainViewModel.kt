package com.cherryzp.cherrypokemon.ui.view.main

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.cherryzp.cherrypokemon.ui.view.base.base.BaseViewModel
import com.cherryzp.domain.usecase.PokemonListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val pokemonListUseCase: PokemonListUseCase
): BaseViewModel<MainUiState>() {
    override val initialState: MainUiState
        get() = MainUiState()

    init {
        fetchData()
    }

    private fun fetchData() = viewModelScope.launch {
        val pokemons = pokemonListUseCase.invoke().cachedIn(viewModelScope)
            .catch { e ->
                e.printStackTrace()
            }
        reduceState { state ->
            state.copy(
                pokemons = pokemons
            )
        }
    }

    fun updateBackgroundColor(pokeId: Int, color: Color) = reduceState {
        Log.e("updateBackgroundColor", "pokeId: $pokeId, color: $color")
        it.copy(
            pokemonBackgroundColor = it
                .pokemonBackgroundColor
                .toMutableMap()
                .apply {
                    put(pokeId, color)
                }.toPersistentMap()
        )
    }

    fun goPokemonDetail(pokeId: Int) = postSideEffect {
        MainUiSideEffect.goPokemonDetail(pokeId)
    }
}