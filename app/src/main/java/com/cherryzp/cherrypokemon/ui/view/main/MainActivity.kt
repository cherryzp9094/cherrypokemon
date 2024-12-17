package com.cherryzp.cherrypokemon.ui.view.main

import androidx.activity.viewModels
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.cherryzp.cherrypokemon.ui.view.base.base.BaseActivity
import com.cherryzp.cherrypokemon.ui.view.base.base.UiSideEffect
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
        )
    }

    override fun handleSideEffect(sideEffect: UiSideEffect) {/**ignore**/}

}
