package com.cherryzp.cherrypokemon.feature.pokemonlist.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.platform.app.InstrumentationRegistry
import com.cherryzp.cherrypokemon.core.designsystem.theme.CherryPokemonTheme
import com.cherryzp.cherrypokemon.core.model.AppLanguage
import com.cherryzp.cherrypokemon.core.model.Pokemon
import com.cherryzp.cherrypokemon.core.ui.R as CoreUiR
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PokemonListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

    @Test
    fun pokemons_areShownWithNumberAndName() {
        composeTestRule.setContent {
            PokemonListScreenWith(
                pagingData = PagingData.from(
                    listOf(Pokemon(id = 1, name = "bulbasaur", imageUrl = "")),
                    loadedStates
                )
            )
        }

        composeTestRule.onNodeWithText("bulbasaur").assertIsDisplayed()
        composeTestRule
            .onNodeWithText(resources.getString(R.string.feature_pokemonlist_impl_title))
            .assertIsDisplayed()
    }

    @Test
    fun pokemonClick_passesPokeId() {
        var clickedId: Int? = null
        composeTestRule.setContent {
            PokemonListScreenWith(
                pagingData = PagingData.from(
                    listOf(Pokemon(id = 25, name = "pikachu", imageUrl = "")),
                    loadedStates
                ),
                onPokemonClick = { clickedId = it }
            )
        }

        composeTestRule.onNodeWithText("pikachu").performClick()

        assertEquals(25, clickedId)
    }

    @Test
    fun emptyList_showsEmptyMessage() {
        composeTestRule.setContent {
            PokemonListScreenWith(pagingData = PagingData.from(emptyList(), loadedStates))
        }

        composeTestRule
            .onNodeWithText(resources.getString(R.string.feature_pokemonlist_impl_empty))
            .assertIsDisplayed()
    }

    @Test
    fun mediatorError_withoutCache_showsRetry() {
        composeTestRule.setContent {
            PokemonListScreenWith(
                pagingData = PagingData.from(
                    data = emptyList(),
                    sourceLoadStates = loadedStates,
                    mediatorLoadStates = LoadStates(
                        refresh = LoadState.Error(IllegalStateException()),
                        prepend = LoadState.NotLoading(endOfPaginationReached = true),
                        append = LoadState.NotLoading(endOfPaginationReached = true)
                    )
                )
            )
        }

        composeTestRule
            .onNodeWithText(resources.getString(R.string.feature_pokemonlist_impl_retry))
            .assertIsDisplayed()
    }

    @Test
    fun languageClick_passesChosenLanguage() {
        var chosen: AppLanguage? = null
        composeTestRule.setContent {
            PokemonListScreenWith(
                pagingData = PagingData.from(
                    listOf(Pokemon(id = 1, name = "bulbasaur", imageUrl = "")),
                    loadedStates
                ),
                language = AppLanguage.ENGLISH,
                onLanguageClick = { chosen = it }
            )
        }

        composeTestRule
            .onNodeWithContentDescription(
                resources.getString(CoreUiR.string.core_ui_language_korean)
            )
            .performClick()

        assertEquals(AppLanguage.KOREAN, chosen)
    }

    @Test
    fun emptyList_stillShowsLanguageToggle() {
        composeTestRule.setContent {
            PokemonListScreenWith(pagingData = PagingData.from(emptyList(), loadedStates))
        }

        composeTestRule
            .onNodeWithContentDescription(
                resources.getString(CoreUiR.string.core_ui_language_english)
            )
            .assertIsDisplayed()
    }

    @Test
    fun typingInSearch_passesQuery() {
        var typed: String? = null
        composeTestRule.setContent {
            PokemonListScreenWith(
                pagingData = PagingData.from(
                    listOf(Pokemon(id = 1, name = "bulbasaur", imageUrl = "")),
                    loadedStates
                ),
                onQueryChange = { typed = it }
            )
        }

        composeTestRule
            .onNodeWithText(resources.getString(CoreUiR.string.core_ui_search_placeholder))
            .performTextInput("pika")

        assertEquals("pika", typed)
    }

    @Test
    fun searchWithNoResult_showsEmptyMessageWithQuery() {
        composeTestRule.setContent {
            PokemonListScreenWith(
                pagingData = PagingData.from(emptyList(), loadedStates),
                query = "없는이름"
            )
        }

        composeTestRule
            .onNodeWithText(
                resources.getString(CoreUiR.string.core_ui_search_empty, "없는이름")
            )
            .assertIsDisplayed()
    }

    @Test
    fun resultsChange_keepsSearchFieldFocused() {
        // 글자를 칠 때마다 검색 결과가 새로 만들어진다. 그때 입력란이 다시 만들어지면 키보드가 닫힌다.
        val pagingData = MutableStateFlow(
            PagingData.from(
                listOf(Pokemon(id = 1, name = "bulbasaur", imageUrl = "")),
                loadedStates
            )
        )
        composeTestRule.setContent {
            val pokemons = pagingData.collectAsLazyPagingItems()
            CherryPokemonTheme {
                PokemonListScreen(
                    pokemons = pokemons,
                    language = AppLanguage.KOREAN,
                    onLanguageClick = {},
                    onPokemonClick = {}
                )
            }
        }
        val searchField = composeTestRule.onNode(hasSetTextAction())
        searchField.performClick()
        searchField.assertIsFocused()

        pagingData.value = PagingData.empty(loadingStates)
        composeTestRule.waitForIdle()
        searchField.assertIsFocused()

        pagingData.value = PagingData.from(emptyList(), loadedStates)
        composeTestRule.waitForIdle()
        searchField.assertIsFocused()
    }

    @Test
    fun resultsEmptyAfterScrolling_showsSearchFieldAgain() {
        val pagingData = MutableStateFlow(
            PagingData.from(
                (1..40).map { Pokemon(id = it, name = "pokemon$it", imageUrl = "") },
                loadedStates
            )
        )
        composeTestRule.setContent {
            val pokemons = pagingData.collectAsLazyPagingItems()
            CherryPokemonTheme {
                PokemonListScreen(
                    pokemons = pokemons,
                    language = AppLanguage.KOREAN,
                    onLanguageClick = {},
                    onPokemonClick = {}
                )
            }
        }
        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(30)
        composeTestRule.onNode(hasSetTextAction()).assertIsNotDisplayed()

        // 검색어를 바꾸면 결과가 로딩을 거쳐 비게 된다. 내려 둔 위치가 남아 검색창이 밀려난 채
        // 안내 글만 보이면 안 된다.
        pagingData.value = PagingData.empty(loadingStates)
        composeTestRule.waitForIdle()
        pagingData.value = PagingData.from(emptyList(), loadedStates)

        composeTestRule.onNode(hasSetTextAction()).assertIsDisplayed()
    }

    @Composable
    private fun PokemonListScreenWith(
        pagingData: PagingData<Pokemon>,
        language: AppLanguage = AppLanguage.KOREAN,
        onLanguageClick: (AppLanguage) -> Unit = {},
        onPokemonClick: (Int) -> Unit = {},
        query: String = "",
        onQueryChange: (String) -> Unit = {},
    ) {
        val pokemons: LazyPagingItems<Pokemon> = flowOf(pagingData).collectAsLazyPagingItems()
        CherryPokemonTheme {
            PokemonListScreen(
                pokemons = pokemons,
                language = language,
                onLanguageClick = onLanguageClick,
                onPokemonClick = onPokemonClick,
                query = query,
                onQueryChange = onQueryChange
            )
        }
    }

    private companion object {
        val loadingStates = LoadStates(
            refresh = LoadState.Loading,
            prepend = LoadState.NotLoading(endOfPaginationReached = false),
            append = LoadState.NotLoading(endOfPaginationReached = false)
        )

        val loadedStates = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = true),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true)
        )
    }
}
