package com.cherryzp.cherrypokemon.feature.pokemonlist.impl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.cherryzp.cherrypokemon.core.designsystem.theme.CherryPokemonTheme
import org.junit.Rule
import org.junit.Test

class DetailPlaceholderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

    @Test
    fun placeholder_tellsWhatToDo() {
        composeTestRule.setContent {
            CherryPokemonTheme { DetailPlaceholder() }
        }

        composeTestRule
            .onNodeWithText(
                resources.getString(R.string.feature_pokemonlist_impl_detail_placeholder_title)
            )
            .assertIsDisplayed()
    }
}
