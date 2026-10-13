package com.cherryzp.cherrypokemon.feature.pokemondetail.impl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.cherryzp.cherrypokemon.core.designsystem.theme.CherryPokemonTheme
import com.cherryzp.cherrypokemon.core.model.LocalizedText
import com.cherryzp.cherrypokemon.core.model.PokemonAbility
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.model.PokemonStats
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.model.PokemonTypeInfo
import com.cherryzp.cherrypokemon.core.model.TypeMatchup
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PokemonDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

    /** 테스트 기기 언어가 한국어가 아니면 영어가 나온다. 어느 쪽이든 하나는 보여야 한다. */
    private val isKorean = resources.configuration.locales[0].language == "ko"

    private val pokemonDetail = PokemonDetail(
        id = 1,
        name = "bulbasaur",
        localizedName = LocalizedText(korean = "이상해씨", english = "Bulbasaur"),
        genus = LocalizedText(korean = "씨앗포켓몬", english = "Seed Pokémon"),
        flavorText = LocalizedText(korean = "씨앗에 담긴 영양을 섭취한다.", english = "A strange seed."),
        imageUrl = "",
        heightMeters = 0.7f,
        weightKilograms = 6.9f,
        types = listOf(
            PokemonTypeInfo(PokemonType.Grass, LocalizedText(korean = "풀", english = "Grass")),
            PokemonTypeInfo(PokemonType.Poison, LocalizedText(korean = "독", english = "Poison"))
        ),
        baseExperience = 64,
        stats = PokemonStats(
            hp = 45,
            attack = 49,
            defense = 49,
            specialAttack = 65,
            specialDefense = 65,
            speed = 45
        ),
        typeMatchups = listOf(
            TypeMatchup(
                type = PokemonTypeInfo(
                    PokemonType.Fire,
                    LocalizedText(korean = "불꽃", english = "Fire")
                ),
                multiplier = 2f
            ),
            TypeMatchup(
                type = PokemonTypeInfo(
                    PokemonType.Water,
                    LocalizedText(korean = "물", english = "Water")
                ),
                multiplier = 0.5f
            )
        ),
        abilities = listOf(
            PokemonAbility(
                name = "overgrow",
                isHidden = false,
                localizedName = LocalizedText(korean = "심록", english = "Overgrow")
            ),
            PokemonAbility(
                name = "chlorophyll",
                isHidden = true,
                localizedName = LocalizedText(korean = "엽록소", english = "Chlorophyll")
            )
        )
    )

    @Test
    fun detail_showsLocalizedNameGenusAndFlavorText() {
        setScreen(PokemonDetailUiState(pokemonDetail, RefreshState.Idle))

        composeTestRule
            .onNodeWithText(if (isKorean) "이상해씨" else "Bulbasaur")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(if (isKorean) "씨앗포켓몬" else "Seed Pokémon")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(if (isKorean) "씨앗에 담긴 영양을 섭취한다." else "A strange seed.")
            .assertIsDisplayed()
    }

    @Test
    fun detail_isShownWithTypesAndMeasurements() {
        setScreen(PokemonDetailUiState(pokemonDetail, RefreshState.Idle))

        composeTestRule.onNodeWithText(if (isKorean) "풀" else "Grass").assertIsDisplayed()
        composeTestRule.onNodeWithText("0.7 m").assertIsDisplayed()
        composeTestRule.onNodeWithText("6.9 kg").assertIsDisplayed()
    }

    @Test
    fun detail_showsStatsWithTotal() {
        setScreen(PokemonDetailUiState(pokemonDetail, RefreshState.Idle))

        composeTestRule
            .onNodeWithContentDescription(
                resources.getString(R.string.feature_pokemondetail_impl_stat_hp) + " 45"
            )
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(
                resources.getString(R.string.feature_pokemondetail_impl_stat_speed) + " 45"
            )
            .assertIsDisplayed()
        // 45 + 49 + 49 + 65 + 65 + 45
        composeTestRule.onNodeWithText("318").assertIsDisplayed()
    }

    @Test
    fun detail_showsWeaknessesAndResistances() {
        setScreen(PokemonDetailUiState(pokemonDetail, RefreshState.Idle))

        composeTestRule
            .onNodeWithText(resources.getString(R.string.feature_pokemondetail_impl_weakness))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(if (isKorean) "불꽃 ×2" else "Fire ×2")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(if (isKorean) "물 ×0.5" else "Water ×0.5")
            .assertIsDisplayed()
    }

    @Test
    fun detail_showsAbilitiesWithHiddenLabel() {
        setScreen(PokemonDetailUiState(pokemonDetail, RefreshState.Idle))

        // 상성까지 들어가 화면이 길어졌다. 특성은 스크롤해야 보인다.
        composeTestRule
            .onNodeWithText(if (isKorean) "심록" else "Overgrow")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(if (isKorean) "엽록소" else "Chlorophyll")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(resources.getString(R.string.feature_pokemondetail_impl_hidden_ability))
            .assertIsDisplayed()
    }

    @Test
    fun failedWithCache_keepsShowingDetail() {
        setScreen(PokemonDetailUiState(pokemonDetail, RefreshState.Failed))

        composeTestRule.onNodeWithText(if (isKorean) "풀" else "Grass").assertIsDisplayed()
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
