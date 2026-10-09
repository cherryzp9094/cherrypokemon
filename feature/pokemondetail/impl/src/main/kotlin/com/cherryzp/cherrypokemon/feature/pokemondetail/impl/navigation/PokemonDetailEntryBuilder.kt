package com.cherryzp.cherrypokemon.feature.pokemondetail.impl.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.cherryzp.cherrypokemon.feature.pokemondetail.api.PokemonDetailNavKey
import com.cherryzp.cherrypokemon.feature.pokemondetail.impl.PokemonDetailScreen
import com.cherryzp.cherrypokemon.feature.pokemondetail.impl.PokemonDetailViewModel

/** 상세 화면 진입점. NavKey 의 도감 번호를 ViewModel 에 넘긴다. */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.pokemonDetailEntryBuilder(
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    entry<PokemonDetailNavKey>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
        PokemonDetailScreen(
            viewModel = hiltViewModel<PokemonDetailViewModel, PokemonDetailViewModel.Factory>(
                key = key.pokeId.toString()
            ) { factory -> factory.create(key.pokeId) },
            onBackClick = onBackClick,
            snackbarHostState = snackbarHostState
        )
    }
}
