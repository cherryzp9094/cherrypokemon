package com.cherryzp.cherrypokemon.feature.pokemonlist.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.android.tools.screenshot.PreviewTest
import com.cherryzp.cherrypokemon.core.designsystem.theme.CherryPokemonTheme
import com.cherryzp.cherrypokemon.core.model.AppLanguage
import com.cherryzp.cherrypokemon.core.ui.FormFactorPreviews
import com.cherryzp.cherrypokemon.core.ui.previewPokemons
import kotlinx.coroutines.flow.MutableStateFlow

/** 목록 화면이 폰·폴드·태블릿에서 어떻게 깔리는지. 열 수나 머리말이 바뀌면 실패한다. */
class PokemonListScreenScreenshotTest {

    @PreviewTest
    @FormFactorPreviews
    @Composable
    fun pokemonList() {
        // 다 받은 상태로 꾸민다. 기본값은 로딩 중이라 빙글이만 찍힌다.
        val loaded = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = false),
            prepend = LoadState.NotLoading(endOfPaginationReached = false),
            append = LoadState.NotLoading(endOfPaginationReached = true)
        )
        // StateFlow 에 담으면 Paging 이 캐시된 데이터로 보고 효과를 기다리지 않고 바로 그린다.
        // 보통 flow 는 LaunchedEffect 안에서 수집되는데, 스크린샷 렌더러에는 메인 디스패처가 없어
        // 영영 로딩 중으로 찍힌다.
        val pokemons = remember {
            MutableStateFlow(PagingData.from(previewPokemons(), sourceLoadStates = loaded))
        }
        CherryPokemonTheme {
            PokemonListScreen(
                pokemons = pokemons.collectAsLazyPagingItems(),
                language = AppLanguage.ENGLISH,
                onLanguageClick = {},
                onPokemonClick = {},
                selectedPokeId = 4
            )
        }
    }
}
