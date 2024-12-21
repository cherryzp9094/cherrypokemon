package com.cherryzp.cherrypokemon.ui.view.main

import android.content.Intent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.cherryzp.cherrypokemon.ui.view.base.base.BaseActivity
import com.cherryzp.cherrypokemon.ui.view.base.base.UiSideEffect
import com.cherryzp.cherrypokemon.ui.view.pokemonDetail.PokemonDetailActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<MainViewModel, MainUiState>() {
    override val viewModel: MainViewModel by viewModels()

    @Composable
    override fun BuildContent(paddingValues: PaddingValues) {
        val uiState by viewModel.container.uiState.collectAsStateWithLifecycle()
        val pokemons = uiState.pokemons?.collectAsLazyPagingItems()

        MainScreen(
            pokemons = pokemons,
            goPokemonDetail = viewModel::goPokemonDetail
        )
    }

    override fun handleSideEffect(sideEffect: UiSideEffect) {
        when (sideEffect) {
            is MainUiSideEffect.goPokemonDetail -> {
                startActivity(
                    Intent(
                        this, PokemonDetailActivity::class.java
                    ).putExtras(
                        PokemonDetailActivity.create(sideEffect.pokeId)
                    )
                )
            }
        }
    }

}
