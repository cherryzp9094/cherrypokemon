package com.cherryzp.cherrypokemon.feature.pokemonlist.impl.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.cherryzp.cherrypokemon.feature.pokemonlist.api.PokemonListNavKey
import com.cherryzp.cherrypokemon.feature.pokemonlist.impl.DetailPlaceholder
import com.cherryzp.cherrypokemon.feature.pokemonlist.impl.PokemonListScreen

/**
 * 목록 화면 진입점. 화면은 `Navigator` 를 받지 않고 콜백만 받는다.
 *
 * @param selectedPokeId 지금 열려 있는 상세의 번호. 두 칸 화면에서 목록이 그 카드를 표시한다.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.pokemonListEntryBuilder(
    selectedPokeId: () -> Int?,
    onPokemonClick: (pokeId: Int) -> Unit,
) {
    // 창이 넓으면 목록이 왼쪽 pane 에 남는다. 아직 고르지 않았으면 오른쪽에 안내를 띄운다.
    entry<PokemonListNavKey>(
        metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { DetailPlaceholder() })
    ) {
        PokemonListScreen(
            viewModel = hiltViewModel(),
            selectedPokeId = selectedPokeId(),
            onPokemonClick = onPokemonClick
        )
    }
}
