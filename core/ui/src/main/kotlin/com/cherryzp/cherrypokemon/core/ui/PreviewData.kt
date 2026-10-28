package com.cherryzp.cherrypokemon.core.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.cherryzp.cherrypokemon.core.model.EvolutionCondition
import com.cherryzp.cherrypokemon.core.model.EvolutionStage
import com.cherryzp.cherrypokemon.core.model.LocalizedText
import com.cherryzp.cherrypokemon.core.model.Pokemon
import com.cherryzp.cherrypokemon.core.model.PokemonAbility
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.model.PokemonStats
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.model.PokemonTypeInfo
import com.cherryzp.cherrypokemon.core.model.TypeMatchup

/** Preview 에서 쓰는 포켓몬 샘플. */
class PokemonPreviewParameterProvider : PreviewParameterProvider<Pokemon> {
    override val values = sequenceOf(
        Pokemon(id = 1, name = "bulbasaur", imageUrl = previewImageUrl(1)),
        Pokemon(id = 4, name = "charmander", imageUrl = previewImageUrl(4))
    )
}

/** 목록 화면 Preview 에 깔 포켓몬. 폴드·태블릿에서도 한 화면이 차도록 여덟 마리. */
fun previewPokemons(): List<Pokemon> = listOf(
    "bulbasaur",
    "ivysaur",
    "venusaur",
    "charmander",
    "charmeleon",
    "charizard",
    "squirtle",
    "wartortle"
).mapIndexed { index, name ->
    Pokemon(id = index + 1, name = name, imageUrl = previewImageUrl(index + 1))
}

/** Preview 에서 쓰는 상세 샘플. */
class PokemonDetailPreviewParameterProvider : PreviewParameterProvider<PokemonDetail> {
    override val values = sequenceOf(
        PokemonDetail(
            id = 1,
            name = "bulbasaur",
            localizedName = LocalizedText(korean = "이상해씨", english = "Bulbasaur"),
            genus = LocalizedText(korean = "씨앗포켓몬", english = "Seed Pokémon"),
            flavorText = LocalizedText(
                korean = "태어나서 얼마 동안 등의 씨앗에 담긴 영양을 섭취하며 자란다.",
                english = "A strange seed was planted on its back at birth."
            ),
            imageUrl = previewImageUrl(1),
            heightMeters = 0.7f,
            weightKilograms = 6.9f,
            types = listOf(
                PokemonTypeInfo(
                    PokemonType.Grass,
                    LocalizedText(korean = "풀", english = "Grass")
                ),
                PokemonTypeInfo(
                    PokemonType.Poison,
                    LocalizedText(korean = "독", english = "Poison")
                )
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
            ),
            // 진화·상성 구간도 Preview 에 나오게 한다. 비우면 그 구간은 그려지지 않는다.
            typeMatchups = listOf(
                TypeMatchup(typeInfo(PokemonType.Fire, "불꽃", "Fire"), multiplier = 2f),
                TypeMatchup(typeInfo(PokemonType.Flying, "비행", "Flying"), multiplier = 2f),
                TypeMatchup(typeInfo(PokemonType.Ice, "얼음", "Ice"), multiplier = 2f),
                TypeMatchup(typeInfo(PokemonType.Psychic, "에스퍼", "Psychic"), multiplier = 2f),
                TypeMatchup(typeInfo(PokemonType.Water, "물", "Water"), multiplier = 0.5f),
                TypeMatchup(typeInfo(PokemonType.Grass, "풀", "Grass"), multiplier = 0.25f),
                TypeMatchup(typeInfo(PokemonType.Electric, "전기", "Electric"), multiplier = 0.5f)
            ),
            evolutionStages = listOf(
                evolutionStage(1, "bulbasaur", "이상해씨", "Bulbasaur", stage = 1, minLevel = null),
                evolutionStage(2, "ivysaur", "이상해풀", "Ivysaur", stage = 2, minLevel = 16),
                evolutionStage(3, "venusaur", "이상해꽃", "Venusaur", stage = 3, minLevel = 32)
            )
        )
    )
}

private fun typeInfo(type: PokemonType, korean: String, english: String) =
    PokemonTypeInfo(type, LocalizedText(korean = korean, english = english))

private fun evolutionStage(
    id: Int,
    name: String,
    korean: String,
    english: String,
    stage: Int,
    minLevel: Int?,
) = EvolutionStage(
    pokemonId = id,
    name = name,
    localizedName = LocalizedText(korean = korean, english = english),
    imageUrl = previewImageUrl(id),
    stage = stage,
    condition = minLevel?.let { EvolutionCondition(minLevel = it, trigger = "level-up") }
)

private fun previewImageUrl(id: Int) =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
