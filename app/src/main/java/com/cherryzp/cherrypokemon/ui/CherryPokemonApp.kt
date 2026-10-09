package com.cherryzp.cherrypokemon.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.cherryzp.cherrypokemon.core.navigation.NavigationState
import com.cherryzp.cherrypokemon.core.navigation.Navigator
import com.cherryzp.cherrypokemon.core.navigation.rememberNavigationState
import com.cherryzp.cherrypokemon.feature.pokemondetail.api.navigateToPokemonDetail
import com.cherryzp.cherrypokemon.feature.pokemondetail.impl.navigation.pokemonDetailEntryBuilder
import com.cherryzp.cherrypokemon.feature.pokemonlist.api.PokemonListNavKey
import com.cherryzp.cherrypokemon.feature.pokemonlist.impl.navigation.pokemonListEntryBuilder

/** 화면들을 모아 back stack 을 그린다. */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun CherryPokemonApp(
    navigationState: NavigationState = rememberNavigationState(startKey = PokemonListNavKey),
) {
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val snackbarHostState = remember { SnackbarHostState() }

    val entryProvider = entryProvider {
        pokemonListEntryBuilder(onPokemonClick = navigator::navigateToPokemonDetail)
        pokemonDetailEntryBuilder(
            onBackClick = navigator::goBack,
            snackbarHostState = snackbarHostState
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
            // 창이 넓으면 목록과 상세를 나란히 보여준다.
            sceneStrategies = listOf(rememberListDetailSceneStrategy()),
            onBack = { navigator.goBack() },
            modifier = Modifier.padding(padding)
        )
    }
}
