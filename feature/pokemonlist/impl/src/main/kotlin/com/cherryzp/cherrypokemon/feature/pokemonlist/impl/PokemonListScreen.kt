package com.cherryzp.cherrypokemon.feature.pokemonlist.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.cherryzp.cherrypokemon.core.model.AppLanguage
import com.cherryzp.cherrypokemon.core.model.Pokemon
import com.cherryzp.cherrypokemon.core.ui.LanguageToggle
import com.cherryzp.cherrypokemon.core.ui.PokemonCard
import com.cherryzp.cherrypokemon.core.ui.PokemonSearchField
import com.cherryzp.cherrypokemon.core.ui.R as CoreUiR
import org.orbitmvi.orbit.compose.collectAsState

/** 화면 좌우 여백. 제목·검색·카드가 모두 같은 선에서 시작하고 끝난다. */
private val ScreenGutter = 16.dp

/** 카드 사이 간격. 색 면끼리 붙으면 경계가 뭉개진다. */
private val CardGap = 12.dp

/** 두 여백을 합친다. `PaddingValues` 끼리는 더할 수 없어 직접 만든다. */
private operator fun PaddingValues.plus(other: PaddingValues): PaddingValues = PaddingValues(
    start = calculateStartPadding(LayoutDirection.Ltr) +
        other.calculateStartPadding(LayoutDirection.Ltr),
    top = calculateTopPadding() + other.calculateTopPadding(),
    end = calculateEndPadding(LayoutDirection.Ltr) + other.calculateEndPadding(LayoutDirection.Ltr),
    bottom = calculateBottomPadding() + other.calculateBottomPadding()
)

@Composable
internal fun PokemonListScreen(
    viewModel: PokemonListViewModel,
    onPokemonClick: (pokeId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.collectAsState()
    // 위에서 이미 고른 언어로 리소스를 읽고 있다. 고른 적이 없으면 기기 언어가 들어 있다.
    val currentLanguage = AppLanguage.fromTag(
        LocalConfiguration.current.locales[0]?.toLanguageTag()
    )

    PokemonListScreen(
        pokemons = viewModel.pokemons.collectAsLazyPagingItems(),
        language = uiState.language ?: currentLanguage,
        query = uiState.query,
        onQueryChange = viewModel::setQuery,
        onLanguageClick = viewModel::setLanguage,
        onPokemonClick = onPokemonClick,
        modifier = modifier
    )
}

@Composable
internal fun PokemonListScreen(
    pokemons: LazyPagingItems<Pokemon>,
    language: AppLanguage,
    onLanguageClick: (AppLanguage) -> Unit,
    onPokemonClick: (pokeId: Int) -> Unit,
    query: String = "",
    onQueryChange: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // 로컬 캐시(source)와 네트워크(mediator)를 나눠 본다. 캐시가 있으면 목록을 보여준다.
    val localRefresh = pokemons.loadState.source.refresh
    val remoteRefresh = pokemons.loadState.mediator?.refresh
    val header: @Composable () -> Unit = {
        ListHeader(
            language = language,
            onLanguageClick = onLanguageClick,
            query = query,
            onQueryChange = onQueryChange
        )
    }

    when {
        localRefresh is LoadState.Loading -> HeaderedBox(header, modifier) {
            CircularProgressIndicator()
        }

        pokemons.itemCount == 0 && remoteRefresh is LoadState.Error -> HeaderedBox(
            header,
            modifier
        ) {
            ErrorContent(onRetryClick = pokemons::retry)
        }

        pokemons.itemCount == 0 -> HeaderedBox(header, modifier) {
            Text(
                text = if (query.isBlank()) {
                    stringResource(R.string.feature_pokemonlist_impl_empty)
                } else {
                    stringResource(CoreUiR.string.core_ui_search_empty, query)
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }

        else -> PokemonGrid(
            pokemons = pokemons,
            onPokemonClick = onPokemonClick,
            header = header,
            modifier = modifier
        )
    }
}

/** 제목과 언어 토글. 앱 바 대신 목록과 함께 스크롤되어 올라간다. */
@Composable
private fun ListHeader(
    language: AppLanguage,
    onLanguageClick: (AppLanguage) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            // 좌우 여백은 그리드와 빈 화면이 정한다. 여기서 또 넣으면 카드와 줄이 어긋난다.
            .padding(bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.feature_pokemonlist_impl_title),
                style = MaterialTheme.typography.headlineSmall
            )
            LanguageToggle(language = language, onLanguageClick = onLanguageClick)
        }
        PokemonSearchField(
            query = query,
            onQueryChange = onQueryChange,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun PokemonGrid(
    pokemons: LazyPagingItems<Pokemon>,
    onPokemonClick: (pokeId: Int) -> Unit,
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        // 두 칸 화면의 목록 칸은 360dp 남짓이다. 160dp 로 잡으면 여백을 빼고 한 열만 나와
        // 카드 하나가 칸을 다 차지한다.
        columns = GridCells.Adaptive(minSize = 150.dp),
        // 아래 제스처 바에 마지막 카드가 가리지 않게 한다.
        contentPadding = WindowInsets.navigationBars.asPaddingValues() +
            PaddingValues(start = ScreenGutter, end = ScreenGutter, bottom = CardGap),
        horizontalArrangement = Arrangement.spacedBy(CardGap),
        verticalArrangement = Arrangement.spacedBy(CardGap),
        modifier = modifier.fillMaxSize()
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) { header() }

        items(
            count = pokemons.itemCount,
            // 안정적인 key 가 없으면 앞에 아이템이 끼어들 때 다른 카드에 상태가 붙는다.
            key = pokemons.itemKey { it.id }
        ) { index ->
            val pokemon = pokemons[index]
            if (pokemon != null) {
                PokemonCard(
                    name = pokemon.name,
                    number = pokemon.id,
                    imageUrl = pokemon.imageUrl,
                    onClick = { onPokemonClick(pokemon.id) }
                )
            }
        }

        when (pokemons.loadState.append) {
            is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                ErrorContent(onRetryClick = pokemons::retry)
            }

            else -> Unit
        }
    }
}

@Composable
private fun ErrorContent(onRetryClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.feature_pokemonlist_impl_error_load),
            style = MaterialTheme.typography.bodyLarge
        )
        Button(onClick = onRetryClick) {
            Text(stringResource(R.string.feature_pokemonlist_impl_retry))
        }
    }
}

/** 목록이 없을 때도 제목과 토글은 보여야 한다. */
@Composable
private fun HeaderedBox(
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize().padding(horizontal = ScreenGutter)) {
        header()
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            content()
        }
    }
}
