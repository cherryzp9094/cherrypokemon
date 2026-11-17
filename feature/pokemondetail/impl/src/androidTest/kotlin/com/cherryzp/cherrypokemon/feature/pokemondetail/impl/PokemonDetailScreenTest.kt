package com.cherryzp.cherrypokemon.feature.pokemondetail.impl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.cherryzp.cherrypokemon.core.designsystem.theme.CherryPokemonTheme
import com.cherryzp.cherrypokemon.core.model.EggGroup
import com.cherryzp.cherrypokemon.core.model.EvolutionCondition
import com.cherryzp.cherrypokemon.core.model.EvolutionStage
import com.cherryzp.cherrypokemon.core.model.LocalizedText
import com.cherryzp.cherrypokemon.core.model.PokemonAbility
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.model.PokemonMove
import com.cherryzp.cherrypokemon.core.model.PokemonStats
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.model.PokemonTypeInfo
import com.cherryzp.cherrypokemon.core.model.TypeMatchup
import org.junit.Assert.assertEquals
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
        evolutionStages = listOf(
            EvolutionStage(
                pokemonId = 1,
                name = "bulbasaur",
                localizedName = LocalizedText(korean = "이상해씨", english = "Bulbasaur"),
                imageUrl = "",
                stage = 1,
                condition = null
            ),
            EvolutionStage(
                pokemonId = 2,
                name = "ivysaur",
                localizedName = LocalizedText(korean = "이상해풀", english = "Ivysaur"),
                imageUrl = "",
                stage = 2,
                condition = EvolutionCondition(minLevel = 16)
            )
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

        // 진화 체인에도 같은 이름이 있어 머리말의 첫 번째만 본다.
        composeTestRule
            .onAllNodesWithText(if (isKorean) "이상해씨" else "Bulbasaur")
            .onFirst()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(if (isKorean) "씨앗포켓몬" else "Seed Pokémon")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(if (isKorean) "씨앗에 담긴 영양을 섭취한다." else "A strange seed.")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun detail_isShownWithTypesAndMeasurements() {
        setScreen(PokemonDetailUiState(pokemonDetail, RefreshState.Idle))

        composeTestRule.onNodeWithText(if (isKorean) "풀" else "Grass").assertIsDisplayed()
        // 좁은 화면에서는 기본 정보가 화면 밖에 있다.
        composeTestRule.onNodeWithText("0.7 m").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("6.9 kg").performScrollTo().assertIsDisplayed()
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
        composeTestRule.onNodeWithText("318").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun evolutionClick_passesThatPokemonId() {
        var clickedId: Int? = null
        setScreen(
            uiState = PokemonDetailUiState(pokemonDetail, RefreshState.Idle),
            onPokemonClick = { clickedId = it }
        )

        composeTestRule
            .onNodeWithText(if (isKorean) "이상해풀" else "Ivysaur")
            .performScrollTo()
            .performClick()

        assertEquals(2, clickedId)
    }

    @Test
    fun evolution_showsLevelCondition() {
        setScreen(PokemonDetailUiState(pokemonDetail, RefreshState.Idle))

        composeTestRule.onNodeWithText("Lv. 16").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun evolution_tradeWithHeldItem_showsItemAndTrade() {
        val slowking = pokemonDetail.copy(
            evolutionStages = listOf(
                stage(79, "slowpoke", "야돈", "Slowpoke", stage = 1),
                stage(
                    199,
                    "slowking",
                    "야도킹",
                    "Slowking",
                    stage = 2,
                    condition = EvolutionCondition(
                        trigger = "trade",
                        heldItem = "kings-rock",
                        heldItemName = LocalizedText(korean = "왕의징표석", english = "King's Rock")
                    )
                )
            )
        )
        setScreen(PokemonDetailUiState(slowking, RefreshState.Idle))

        val heldItem = if (isKorean) "왕의징표석" else "King's Rock"
        composeTestRule
            .onNodeWithText(
                resources.getString(
                    R.string.feature_pokemondetail_impl_evolution_trade_holding,
                    heldItem
                )
            )
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun evolution_branches_showEachOwnCondition() {
        // 이브이처럼 갈래가 여럿이면 화살표 하나에 첫 조건만 적지 않고 각자 아래에 적는다.
        val eevee = pokemonDetail.copy(
            evolutionStages = listOf(
                stage(133, "eevee", "이브이", "Eevee", stage = 1),
                stage(
                    134,
                    "vaporeon",
                    "샤미드",
                    "Vaporeon",
                    stage = 2,
                    condition = EvolutionCondition(
                        item = "water-stone",
                        itemName = LocalizedText(korean = "물의돌", english = "Water Stone")
                    )
                ),
                stage(
                    197,
                    "umbreon",
                    "블래키",
                    "Umbreon",
                    stage = 2,
                    condition = EvolutionCondition(minHappiness = 160, timeOfDay = "night")
                )
            )
        )
        setScreen(PokemonDetailUiState(eevee, RefreshState.Idle))

        composeTestRule
            .onNodeWithText(if (isKorean) "물의돌" else "Water Stone")
            .performScrollTo()
            .assertIsDisplayed()
        val friendship = resources.getString(
            R.string.feature_pokemondetail_impl_evolution_friendship
        )
        val night = resources.getString(R.string.feature_pokemondetail_impl_evolution_night)
        composeTestRule
            .onNodeWithText("$friendship · $night")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun ability_showsDescription() {
        val withDescription = pokemonDetail.copy(
            abilities = listOf(
                PokemonAbility(
                    name = "overgrow",
                    isHidden = false,
                    localizedName = LocalizedText(korean = "심록", english = "Overgrow"),
                    description = LocalizedText(
                        korean = "HP가 줄었을 때 풀타입 기술의 위력이 올라간다.",
                        english = "Powers up Grass-type moves when the Pokémon's HP is low."
                    )
                )
            )
        )
        setScreen(PokemonDetailUiState(withDescription, RefreshState.Idle))

        composeTestRule
            .onNodeWithText(
                if (isKorean) {
                    "HP가 줄었을 때 풀타입 기술의 위력이 올라간다."
                } else {
                    "Powers up Grass-type moves when the Pokémon's HP is low."
                }
            )
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun header_withShinyAndCry_showsBothButtons() {
        val withExtras = pokemonDetail.copy(
            shinyImageUrl = "https://example.com/shiny/1.png",
            cryUrl = "https://example.com/1.ogg"
        )
        setScreen(PokemonDetailUiState(withExtras, RefreshState.Idle))

        val shiny = composeTestRule.onNodeWithContentDescription(
            resources.getString(R.string.feature_pokemondetail_impl_shiny)
        )
        shiny.assertIsOff()
        shiny.performClick()
        shiny.assertIsOn()
        composeTestRule
            .onNodeWithContentDescription(
                resources.getString(R.string.feature_pokemondetail_impl_play_cry)
            )
            .assertIsDisplayed()
    }

    @Test
    fun header_withoutShinyOrCry_hidesButtons() {
        setScreen(PokemonDetailUiState(pokemonDetail, RefreshState.Idle))

        composeTestRule
            .onNodeWithContentDescription(
                resources.getString(R.string.feature_pokemondetail_impl_shiny)
            )
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithContentDescription(
                resources.getString(R.string.feature_pokemondetail_impl_play_cry)
            )
            .assertDoesNotExist()
    }

    @Test
    fun basics_showCaptureRateGenderAndEggGroups() {
        val withBasics = pokemonDetail.copy(
            captureRate = 190,
            genderRate = 1,
            eggGroups = listOf(
                EggGroup("monster", LocalizedText(korean = "괴수", english = "Monster")),
                EggGroup("plant", LocalizedText(korean = "식물", english = "Grass"))
            )
        )
        setScreen(PokemonDetailUiState(withBasics, RefreshState.Idle))

        composeTestRule.onNodeWithText("190").performScrollTo().assertIsDisplayed()
        // 8 분의 1 이 암컷이면 87.5 : 12.5 다.
        composeTestRule
            .onNodeWithText(
                resources.getString(
                    R.string.feature_pokemondetail_impl_gender_value,
                    "87.5",
                    "12.5"
                )
            )
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(if (isKorean) "괴수 · 식물" else "Monster · Grass")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun basics_genderless_saysSo() {
        setScreen(PokemonDetailUiState(pokemonDetail.copy(genderRate = -1), RefreshState.Idle))

        composeTestRule
            .onNodeWithText(resources.getString(R.string.feature_pokemondetail_impl_genderless))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun legendary_isShownNextToGenus() {
        setScreen(PokemonDetailUiState(pokemonDetail.copy(isLegendary = true), RefreshState.Idle))

        val genus = if (isKorean) "씨앗포켓몬" else "Seed Pokémon"
        val legendary = resources.getString(R.string.feature_pokemondetail_impl_legendary)
        composeTestRule.onNodeWithText("$genus · $legendary").assertIsDisplayed()
    }

    @Test
    fun moves_showLevelNameAndPower() {
        val withMoves = pokemonDetail.copy(
            moves = listOf(
                PokemonMove(
                    name = "vine-whip",
                    localizedName = LocalizedText(korean = "덩굴채찍", english = "Vine Whip"),
                    type = PokemonTypeInfo(
                        PokemonType.Grass,
                        LocalizedText(korean = "풀", english = "Grass")
                    ),
                    power = 77,
                    accuracy = 100,
                    pp = 25,
                    damageClass = "physical",
                    level = 3
                ),
                PokemonMove(
                    name = "growth",
                    localizedName = LocalizedText(korean = "성장", english = "Growth"),
                    type = PokemonTypeInfo(
                        PokemonType.Normal,
                        LocalizedText(korean = "노말", english = "Normal")
                    ),
                    power = null,
                    accuracy = null,
                    pp = 20,
                    damageClass = "status",
                    level = 0
                )
            )
        )
        setScreen(PokemonDetailUiState(withMoves, RefreshState.Idle))

        composeTestRule
            .onNodeWithText(if (isKorean) "덩굴채찍" else "Vine Whip")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("77").performScrollTo().assertIsDisplayed()
        // 변화 기술은 위력 대신 줄표, 진화할 때 배우는 기술은 레벨 대신 "진화".
        composeTestRule.onNodeWithText("—").performScrollTo().assertIsDisplayed()
        composeTestRule
            .onNodeWithText(
                resources.getString(R.string.feature_pokemondetail_impl_move_on_evolution)
            )
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun moves_empty_hidesSection() {
        setScreen(PokemonDetailUiState(pokemonDetail, RefreshState.Idle))

        composeTestRule
            .onNodeWithText(resources.getString(R.string.feature_pokemondetail_impl_section_moves))
            .assertDoesNotExist()
    }

    @Test
    fun evolution_knownMove_showsMoveName() {
        val tangrowth = pokemonDetail.copy(
            evolutionStages = listOf(
                stage(114, "tangela", "덩쿠리", "Tangela", stage = 1),
                stage(
                    465,
                    "tangrowth",
                    "덩쿠림보",
                    "Tangrowth",
                    stage = 2,
                    condition = EvolutionCondition(
                        trigger = "level-up",
                        knownMove = "ancient-power",
                        knownMoveName = LocalizedText(korean = "원시의힘", english = "Ancient Power")
                    )
                )
            )
        )
        setScreen(PokemonDetailUiState(tangrowth, RefreshState.Idle))

        composeTestRule
            .onNodeWithText(
                resources.getString(
                    R.string.feature_pokemondetail_impl_evolution_knows_move,
                    if (isKorean) "원시의힘" else "Ancient Power"
                )
            )
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun detail_showsWeaknessesAndResistances() {
        setScreen(PokemonDetailUiState(pokemonDetail, RefreshState.Idle))

        composeTestRule
            .onNodeWithText(resources.getString(R.string.feature_pokemondetail_impl_weakness))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(if (isKorean) "불꽃 ×2" else "Fire ×2")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(if (isKorean) "물 ×0.5" else "Water ×0.5")
            .performScrollTo()
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
            .performScrollTo()
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

    @Test
    fun twoPane_hidesBackButton() {
        setScreen(
            uiState = PokemonDetailUiState(pokemonDetail, RefreshState.Idle),
            showBackButton = false
        )

        composeTestRule
            .onNodeWithContentDescription(
                resources.getString(R.string.feature_pokemondetail_impl_back)
            )
            .assertDoesNotExist()
    }

    @Test
    fun twoPane_loading_hidesBackButton() {
        setScreen(
            uiState = PokemonDetailUiState(pokemonDetail = null, RefreshState.Refreshing),
            showBackButton = false
        )

        composeTestRule
            .onNodeWithContentDescription(
                resources.getString(R.string.feature_pokemondetail_impl_back)
            )
            .assertDoesNotExist()
    }

    private fun stage(
        id: Int,
        name: String,
        korean: String,
        english: String,
        stage: Int,
        condition: EvolutionCondition? = null,
    ) = EvolutionStage(
        pokemonId = id,
        name = name,
        localizedName = LocalizedText(korean = korean, english = english),
        imageUrl = "",
        stage = stage,
        condition = condition
    )

    private fun setScreen(
        uiState: PokemonDetailUiState,
        onRetryClick: () -> Unit = {},
        onBackClick: () -> Unit = {},
        onPokemonClick: (Int) -> Unit = {},
        showBackButton: Boolean = true,
    ) {
        composeTestRule.setContent {
            CherryPokemonTheme {
                PokemonDetailScreen(
                    uiState = uiState,
                    onRetryClick = onRetryClick,
                    onBackClick = onBackClick,
                    onPokemonClick = onPokemonClick,
                    showBackButton = showBackButton
                )
            }
        }
    }
}
