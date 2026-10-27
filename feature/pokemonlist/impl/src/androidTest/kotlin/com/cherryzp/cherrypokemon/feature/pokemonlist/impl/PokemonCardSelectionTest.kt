package com.cherryzp.cherrypokemon.feature.pokemonlist.impl

import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.cherryzp.cherrypokemon.core.designsystem.theme.CherryPokemonTheme
import com.cherryzp.cherrypokemon.core.ui.PokemonCard
import org.junit.Rule
import org.junit.Test

/** 두 칸 화면에서 오른쪽에 열린 포켓몬을 목록이 접근성 정보로도 알려 주는지 본다. */
class PokemonCardSelectionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun selectedCard_isMarkedSelected() {
        composeTestRule.setContent {
            CherryPokemonTheme {
                PokemonCard(name = "ivysaur", number = 2, imageUrl = "", onClick = {
                }, selected = true)
            }
        }

        composeTestRule.onNodeWithText("ivysaur", useUnmergedTree = false).assertIsSelected()
    }

    @Test
    fun otherCard_isNotSelected() {
        composeTestRule.setContent {
            CherryPokemonTheme {
                PokemonCard(name = "bulbasaur", number = 1, imageUrl = "", onClick = {})
            }
        }

        composeTestRule.onNodeWithText("bulbasaur").assertIsNotSelected()
    }
}
