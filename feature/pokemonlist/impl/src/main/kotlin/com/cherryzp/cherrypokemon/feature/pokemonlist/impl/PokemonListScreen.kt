package com.cherryzp.cherrypokemon.feature.pokemonlist.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
import com.cherryzp.cherrypokemon.core.ui.StatusBarProtection
import org.orbitmvi.orbit.compose.collectAsState

/** 화면 좌우 여백. 제목·검색·카드가 모두 같은 선에서 시작하고 끝난다. */
private val ScreenGutter = 16.dp

/** 카드 사이 간격. 색 면끼리 붙으면 경계가 뭉개진다. */
private val CardGap = 12.dp

/**
 * 두 여백을 합친다. `PaddingValues` 끼리는 더할 수 없어 직접 만든다.
 *
 * 값은 레이아웃 단계에서 읽는다. 여기서 바로 계산하면 키보드 inset 을 컴포지션에서 읽게 되어,
 * 키보드가 열리고 닫히는 동안 그리드가 매 프레임 다시 컴포지션된다.
 */
private operator fun PaddingValues.plus(other: PaddingValues): PaddingValues =
    SumPaddingValues(this, other)

private class SumPaddingValues(
    private val first: PaddingValues,
    private val second: PaddingValues,
) : PaddingValues {
    override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
        first.calculateLeftPadding(layoutDirection) + second.calculateLeftPadding(layoutDirection)

    override fun calculateTopPadding(): Dp =
        first.calculateTopPadding() + second.calculateTopPadding()

    override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
        first.calculateRightPadding(layoutDirection) +
            second.calculateRightPadding(layoutDirection)

    override fun calculateBottomPadding(): Dp =
        first.calculateBottomPadding() + second.calculateBottomPadding()
}

@Composable
internal fun PokemonListScreen(
    viewModel: PokemonListViewModel,
    selectedPokeId: Int?,
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
        selectedPokeId = selectedPokeId,
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
    selectedPokeId: Int? = null,
    modifier: Modifier = Modifier,
) {
    val header: @Composable () -> Unit = {
        ListHeader(
            language = language,
            onLanguageClick = onLanguageClick,
            query = query,
            onQueryChange = onQueryChange
        )
    }

    Box(modifier = modifier) {
        PokemonListBody(
            pokemons = pokemons,
            language = language,
            header = header,
            query = query,
            onPokemonClick = onPokemonClick,
            selectedPokeId = selectedPokeId
        )
        // 머리말이 스크롤되어 올라가면 카드가 상태 바 아이콘과 겹친다.
        StatusBarProtection(modifier = Modifier.align(Alignment.TopCenter))
    }
}

/**
 * 목록의 상태별 화면. 로딩·빈 결과·에러도 그리드 안의 칸으로 그린다.
 *
 * 상태마다 다른 레이아웃으로 바꾸면 검색창이 든 머리말이 새로 만들어진다. 글자를 칠 때마다
 * 검색 결과가 잠깐 로딩이 되므로, 그때마다 입력란이 포커스를 잃고 키보드가 닫힌다.
 */
@Composable
private fun PokemonListBody(
    pokemons: LazyPagingItems<Pokemon>,
    language: AppLanguage,
    header: @Composable () -> Unit,
    query: String,
    onPokemonClick: (pokeId: Int) -> Unit,
    selectedPokeId: Int?,
) {
    // 카드마다 이미지에서 뽑은 대표 색. 카드 밖에 둬야 스크롤로 다시 보이거나 상세에 다녀와도
    // 남는다. 없으면 카드가 회색으로 그려졌다가 다시 칠해진다.
    val cardColors = rememberSaveable(saver = CardColorsSaver) { mutableStateMapOf() }

    // 로컬 캐시(source)와 네트워크(mediator)를 나눠 본다. 캐시가 있으면 목록을 보여준다.
    val localRefresh = pokemons.loadState.source.refresh
    val remoteRefresh = pokemons.loadState.mediator?.refresh

    val placeholder: (@Composable () -> Unit)? = when {
        localRefresh is LoadState.Loading -> {
            { CircularProgressIndicator() }
        }

        pokemons.itemCount == 0 && remoteRefresh is LoadState.Error -> {
            { ErrorContent(onRetryClick = pokemons::retry) }
        }

        pokemons.itemCount == 0 -> {
            {
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
        }

        else -> null
    }

    PokemonGrid(
        pokemons = pokemons,
        language = language,
        cardColors = cardColors,
        onPokemonClick = onPokemonClick,
        selectedPokeId = selectedPokeId,
        header = header,
        placeholder = placeholder
    )
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
    language: AppLanguage,
    cardColors: SnapshotStateMap<Int, Color>,
    onPokemonClick: (pokeId: Int) -> Unit,
    selectedPokeId: Int?,
    header: @Composable () -> Unit,
    placeholder: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()
    // 검색어가 바뀌면 결과가 잠깐 로딩이 된다. 내려 둔 위치가 남으면 머리말(검색창)이 화면 위로
    // 밀려난 채 안내 칸만 보이므로 맨 위로 돌린다. 새 결과도 처음부터 보인다.
    val showsPlaceholder = placeholder != null
    LaunchedEffect(showsPlaceholder) {
        if (showsPlaceholder) gridState.scrollToItem(0)
    }
    LazyVerticalGrid(
        state = gridState,
        // 두 칸 화면의 목록 칸은 360dp 남짓이다. 160dp 로 잡으면 여백을 빼고 한 열만 나와
        // 카드 하나가 칸을 다 차지한다.
        columns = GridCells.Adaptive(minSize = 150.dp),
        // 아래 제스처 바와 키보드에 마지막 카드가 가리지 않게 한다. 창은 키보드에 맞춰 줄지 않으므로
        // 여기서 비워 두지 않으면 검색 중에 마지막 줄까지 올릴 수 없다.
        contentPadding = WindowInsets.navigationBars.union(WindowInsets.ime).asPaddingValues() +
            PaddingValues(start = ScreenGutter, end = ScreenGutter, bottom = CardGap),
        horizontalArrangement = Arrangement.spacedBy(CardGap),
        verticalArrangement = Arrangement.spacedBy(CardGap),
        modifier = modifier.fillMaxSize()
    ) {
        // 같은 key 로 두어야 아래 칸이 바뀌어도 머리말(검색창)이 그대로 남는다.
        item(key = HEADER_KEY, span = { GridItemSpan(maxLineSpan) }) { header() }

        if (placeholder != null) {
            item(key = PLACEHOLDER_KEY, span = { GridItemSpan(maxLineSpan) }) {
                PlaceholderCell(gridState = gridState, content = placeholder)
            }
            return@LazyVerticalGrid
        }

        items(
            count = pokemons.itemCount,
            // 안정적인 key 가 없으면 앞에 아이템이 끼어들 때 다른 카드에 상태가 붙는다.
            key = pokemons.itemKey { it.id }
        ) { index ->
            val pokemon = pokemons[index]
            if (pokemon != null) {
                PokemonCard(
                    // 이름을 아직 받지 못했으면(첫 실행에 오프라인) 식별자라도 보여준다.
                    name = pokemon.localizedName.of(language).ifEmpty { pokemon.name },
                    number = pokemon.id,
                    imageUrl = pokemon.imageUrl,
                    onClick = { onPokemonClick(pokemon.id) },
                    selected = pokemon.id == selectedPokeId,
                    backgroundColor = cardColors[pokemon.id],
                    onBackgroundColorExtract = { color ->
                        // 캐시된 이미지를 다시 그릴 때도 불린다. 같은 색이면 쓰지 않아 다시 그리지 않는다.
                        if (cardColors[pokemon.id] != color) cardColors[pokemon.id] = color
                    }
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

/**
 * 머리말 아래 남은 화면의 가운데에 [content] 를 둔다. 키보드가 열려 있으면 그 위 영역의 가운데다.
 *
 * 남은 높이는 그리드가 잰 값으로 구한다. 그리드 안의 칸은 부모 높이를 채울 수 없다.
 */
@Composable
private fun PlaceholderCell(gridState: LazyGridState, content: @Composable () -> Unit) {
    val layoutInfo = gridState.layoutInfo
    val headerBottom = layoutInfo.visibleItemsInfo
        .firstOrNull { it.key == HEADER_KEY }
        ?.let { it.offset.y + it.size.height + layoutInfo.mainAxisItemSpacing }
        ?: 0
    val remaining = layoutInfo.viewportSize.height - headerBottom - layoutInfo.afterContentPadding
    val minHeight = with(LocalDensity.current) { PLACEHOLDER_MIN_HEIGHT.roundToPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(with(LocalDensity.current) { remaining.coerceAtLeast(minHeight).toDp() }),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/** 대표 색을 회전·프로세스 종료 뒤에도 남긴다. 번호와 ARGB 를 번갈아 담는다. */
internal val CardColorsSaver = Saver<SnapshotStateMap<Int, Color>, IntArray>(
    save = { colors ->
        IntArray(colors.size * 2).also { saved ->
            colors.entries.forEachIndexed { index, (id, color) ->
                saved[index * 2] = id
                saved[index * 2 + 1] = color.toArgb()
            }
        }
    },
    restore = { saved ->
        mutableStateMapOf<Int, Color>().apply {
            for (index in saved.indices step 2) put(saved[index], Color(saved[index + 1]))
        }
    }
)

private const val HEADER_KEY = "header"
private const val PLACEHOLDER_KEY = "placeholder"

/** 키보드가 화면 대부분을 가려도 안내 글이 들어갈 높이. */
private val PLACEHOLDER_MIN_HEIGHT = 160.dp
