package com.cherryzp.cherrypokemon.ui.view.pokemonDetail

import androidx.activity.viewModels
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cherryzp.cherrypokemon.ui.view.base.base.BaseActivity
import com.cherryzp.cherrypokemon.ui.view.base.base.UiSideEffect
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PokemonDetailActivity: BaseActivity<PokemonDetailViewModel, PokemonDetailUiState>() {
    override val viewModel: PokemonDetailViewModel by viewModels()

    @Composable
    override fun BuildContent(paddingValues: PaddingValues) {
        val uiState by viewModel.container.uiState.collectAsStateWithLifecycle()
        uiState.pokemonDetail?.let { pokemonDetail ->
            PokemonDetailScreen(pokemonDetail = pokemonDetail)
        }
    }

    override fun handleSideEffect(sideEffect: UiSideEffect) {}
}