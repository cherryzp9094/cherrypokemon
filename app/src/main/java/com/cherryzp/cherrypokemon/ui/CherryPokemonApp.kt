package com.cherryzp.cherrypokemon.ui

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.cherryzp.cherrypokemon.core.navigation.NavigationState
import com.cherryzp.cherrypokemon.core.navigation.Navigator
import com.cherryzp.cherrypokemon.core.navigation.rememberNavigationState
import com.cherryzp.cherrypokemon.feature.pokemonlist.api.PokemonListNavKey
import com.cherryzp.cherrypokemon.feature.pokemonlist.impl.navigation.pokemonListEntryBuilder
import com.cherryzp.cherrypokemon.ui.view.pokemonDetail.PokemonDetailActivity

/** 화면들을 모아 back stack 을 그린다. */
@Composable
fun CherryPokemonApp(
    navigationState: NavigationState = rememberNavigationState(startKey = PokemonListNavKey),
) {
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val entryProvider = entryProvider {
        pokemonListEntryBuilder(
            onPokemonClick = { pokeId ->
                // 과도기 코드: 상세 화면을 옮기는 6-2 에서 지운다.
                context.startActivity(
                    Intent(context, PokemonDetailActivity::class.java)
                        .putExtras(PokemonDetailActivity.create(pokeId, Color.White.toArgb()))
                )
            }
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        NavDisplay(
            backStack = navigationState.backStack,
            entryProvider = entryProvider,
            entryDecorators = listOf(
                // SaveableStateHolder 를 첫 번째로 둔다.
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            onBack = { navigator.goBack() },
            modifier = Modifier.padding(padding)
        )
    }
}
