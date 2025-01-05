package com.cherryzp.cherrypokemon.ui.view.pokemonDetail

import androidx.activity.viewModels
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.os.bundleOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cherryzp.cherrypokemon.ui.view.base.base.BaseActivity
import com.cherryzp.cherrypokemon.ui.view.base.base.UiSideEffect
import com.cherryzp.consts.KeyConsts.POKEMON_BACKGROUND_COLOR
import com.cherryzp.consts.KeyConsts.POKE_NO
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PokemonDetailActivity: BaseActivity<PokemonDetailViewModel, PokemonDetailUiState>() {
    override val viewModel: PokemonDetailViewModel by viewModels()

    @Composable
    override fun BuildContent(paddingValues: PaddingValues) {
        val uiState by viewModel.container.uiState.collectAsStateWithLifecycle()
        uiState.pokemonDetail?.let { pokemonDetail ->
            PokemonDetailScreen(
                paddingValues = paddingValues,
                window = window,
                pokemonDetail = pokemonDetail,
                pokemonBackgroundColor = uiState.pokemonBackgroundColor
            )
        }
    }

    override fun handleSideEffect(sideEffect: UiSideEffect) {}

    companion object {
        fun create(
            pokeId: Int,
            pokemonBackgroundColor: Int
        ) = bundleOf(
            POKE_NO to pokeId,
            POKEMON_BACKGROUND_COLOR to pokemonBackgroundColor
        )
    }
}