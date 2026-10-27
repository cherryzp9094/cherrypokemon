package com.cherryzp.cherrypokemon.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.cherryzp.cherrypokemon.core.navigation.NavigationState
import com.cherryzp.cherrypokemon.core.navigation.Navigator
import com.cherryzp.cherrypokemon.core.navigation.rememberNavigationState
import com.cherryzp.cherrypokemon.feature.pokemondetail.api.PokemonDetailNavKey
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
        pokemonListEntryBuilder(
            // 맨 위가 상세면 그 번호를 목록에 알려 준다. 한 칸 화면에서는 목록이 가려져 있어 상관없다.
            selectedPokeId = { (navigationState.currentKey as? PokemonDetailNavKey)?.pokeId },
            onPokemonClick = navigator::navigateToPokemonDetail
        )
        pokemonDetailEntryBuilder(
            onBackClick = navigator::goBack,
            // 진화 체인에서 다른 포켓몬으로 간다. 뒤로가기로 돌아온다.
            onPokemonClick = navigator::navigateToPokemonDetail,
            snackbarHostState = snackbarHostState
        )
    }

    Scaffold(
        // 화면 여백을 꺼 두었으므로 스낵바는 내비게이션 바 위로 직접 올린다.
        snackbarHost = {
            SnackbarHost(snackbarHostState, modifier = Modifier.navigationBarsPadding())
        },
        // 화면마다 제 여백을 넣는다. 여기서 또 넣으면 상태 바 아래가 두 번 밀린다.
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        NavDisplay(
            backStack = navigationState.backStack,
            entryProvider = entryProvider,
            entryDecorators = listOf(
                // SaveableStateHolder 를 첫 번째로 둔다.
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            // 창이 넓으면 목록과 상세를 나란히 보여준다.
            sceneStrategies = listOf(rememberListDetailSceneStrategy(directive = paneDirective())),
            onBack = { navigator.goBack() },
            modifier = Modifier.padding(padding)
        )
    }
}

/**
 * 몇 칸으로 나눌지 정한다.
 *
 * Material 기본값은 창 너비 840dp 부터 두 칸이다. 그러면 펼친 폴드(약 823dp)는 한 칸이 되어
 * 폰 가로(923dp)보다 큰 화면이 더 나쁘게 나온다. 목록(약 320dp)과 상세(약 400dp)가 함께
 * 들어가는 720dp 부터 두 칸으로 나눈다.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun paneDirective(): PaneScaffoldDirective {
    val default = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
    val widthDp = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() }
    return if (widthDp >= TWO_PANE_MIN_WIDTH && default.maxHorizontalPartitions < 2) {
        default.copy(maxHorizontalPartitions = 2)
    } else {
        default
    }
}

private val TWO_PANE_MIN_WIDTH = 720.dp
