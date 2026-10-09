package com.cherryzp.cherrypokemon

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import com.cherryzp.cherrypokemon.core.model.Pokemon
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.testing.repository.FakePokemonRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** 시작 화면과 목록 → 상세 → 뒤로가기 경로를 확인한다. */
@HiltAndroidTest
class NavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var pokemonRepository: FakePokemonRepository

    private val bulbasaur = Pokemon(id = 1, name = "bulbasaur", imageUrl = "")

    @Before
    fun setUp() {
        hiltRule.inject()
        pokemonRepository.sendPokemons(listOf(bulbasaur))
        pokemonRepository.sendPokemonDetail(
            PokemonDetail(
                id = 1,
                name = "bulbasaur",
                imageUrl = "",
                heightMeters = 0.7f,
                weightKilograms = 6.9f,
                types = listOf(PokemonType.Grass)
            )
        )
    }

    @Test
    fun startDestination_isPokemonList() {
        composeTestRule.onNodeWithText("Cherry Pokemon").assertIsDisplayed()
        composeTestRule.onNodeWithText("bulbasaur").assertIsDisplayed()
    }

    @Test
    fun pokemonClick_opensDetail_andBackReturnsToList() {
        composeTestRule.onNodeWithText("bulbasaur").performClick()

        composeTestRule.onNodeWithText("Grass").assertIsDisplayed()
        composeTestRule.onNodeWithText("0.7 m").assertIsDisplayed()

        Espresso.pressBack()

        composeTestRule.onNodeWithText("Cherry Pokemon").assertIsDisplayed()
    }
}
