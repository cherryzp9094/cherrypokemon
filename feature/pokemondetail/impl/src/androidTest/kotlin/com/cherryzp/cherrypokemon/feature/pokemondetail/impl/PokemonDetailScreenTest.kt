package com.cherryzp.cherrypokemon.feature.pokemondetail.impl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.cherryzp.cherrypokemon.core.designsystem.theme.CherryPokemonTheme
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.model.PokemonType
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PokemonDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

    private val pokemonDetail = PokemonDetail(
        id = 1,
        name = "bulbasaur",
        imageUrl = "",
        heightMeters = 0.7f,
        weightKilograms = 6.9f,
        types = listOf(PokemonType.Grass, PokemonType.Poison)
    )

    @Test
    fun detail_isShownWithTypesAndMeasurements() {
        setScreen(PokemonDetailUiState(pokemonDetail, RefreshState.Idle))

        composeTestRule.onNodeWithText("bulbasaur").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grass").assertIsDisplayed()
        composeTestRule.onNodeWithText("0.7 m").assertIsDisplayed()
        composeTestRule.onNodeWithText("6.9 kg").assertIsDisplayed()
    }

    @Test
    fun failedWithCache_keepsShowingDetail() {
        setScreen(PokemonDetailUiState(pokemonDetail, RefreshState.Failed))

        composeTestRule.onNodeWithText("bulbasaur").assertIsDisplayed()
        composeTestRule
            .onNodeWithText(resources.getString(R.string.feature_pokemondetail_impl_retry))
            .assertDoesNotExist()
    }

    @Test
    fun failedWithoutCache_showsRetry_callsOnRetryClick() {
        var retried = false
        setScreen(
            uiState = PokemonDetailUiState(pokemonDetail = null, RefreshState.Failed),
            onRetryClick = { retried = true }
        )

        composeTestRule
            .onNodeWithText(resources.getString(R.string.feature_pokemondetail_impl_retry))
            .performClick()

        assertTrue(retried)
    }

    @Test
    fun backClick_callsOnBackClick() {
        var backed = false
        setScreen(
            uiState = PokemonDetailUiState(pokemonDetail, RefreshState.Idle),
            onBackClick = { backed = true }
        )

        composeTestRule
            .onNodeWithContentDescription(
                resources.getString(R.string.feature_pokemondetail_impl_back)
            )
            .performClick()

        assertTrue(backed)
    }

    private fun setScreen(
        uiState: PokemonDetailUiState,
        onRetryClick: () -> Unit = {},
        onBackClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            CherryPokemonTheme {
                PokemonDetailScreen(
                    uiState = uiState,
                    onRetryClick = onRetryClick,
                    onBackClick = onBackClick
                )
            }
        }
    }
}
