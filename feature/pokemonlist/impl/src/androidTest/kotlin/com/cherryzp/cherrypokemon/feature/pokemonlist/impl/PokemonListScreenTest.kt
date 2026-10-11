package com.cherryzp.cherrypokemon.feature.pokemonlist.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

    @Composable
    private fun PokemonListScreenWith(
        pagingData: PagingData<Pokemon>,
        language: AppLanguage = AppLanguage.KOREAN,
        onLanguageClick: (AppLanguage) -> Unit = {},
        onPokemonClick: (Int) -> Unit = {},
    ) {
        val pokemons: LazyPagingItems<Pokemon> = flowOf(pagingData).collectAsLazyPagingItems()
        CherryPokemonTheme {
            PokemonListScreen(
                pokemons = pokemons,
                language = language,
                onLanguageClick = onLanguageClick,
                onPokemonClick = onPokemonClick
            )
        }
    }

    private companion object {
        val loadedStates = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = true),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true)
        )
    }
}
