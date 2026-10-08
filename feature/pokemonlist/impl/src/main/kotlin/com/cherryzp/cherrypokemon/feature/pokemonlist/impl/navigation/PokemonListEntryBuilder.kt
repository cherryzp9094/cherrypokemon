package com.cherryzp.cherrypokemon.feature.pokemonlist.impl.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.cherryzp.cherrypokemon.feature.pokemonlist.api.PokemonListNavKey
import com.cherryzp.cherrypokemon.feature.pokemonlist.impl.PokemonListScreen

/** 목록 화면 진입점. 화면은 `Navigator` 를 받지 않고 콜백만 받는다. */
fun EntryProviderScope<NavKey>.pokemonListEntryBuilder(onPokemonClick: (pokeId: Int) -> Unit) {
    entry<PokemonListNavKey> {
        PokemonListScreen(
            viewModel = hiltViewModel(),
            onPokemonClick = onPokemonClick
        )
    }
}
