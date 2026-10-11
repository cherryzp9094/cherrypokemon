package com.cherryzp.cherrypokemon.core.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.cherryzp.cherrypokemon.core.model.LocalizedText
import com.cherryzp.cherrypokemon.core.model.Pokemon
import com.cherryzp.cherrypokemon.core.model.PokemonAbility
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.model.PokemonStats
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.model.PokemonTypeInfo

/** Preview 에서 쓰는 포켓몬 샘플. */
class PokemonPreviewParameterProvider : PreviewParameterProvider<Pokemon> {
    override val values = sequenceOf(
        Pokemon(id = 1, name = "bulbasaur", imageUrl = previewImageUrl(1)),
        Pokemon(id = 4, name = "charmander", imageUrl = previewImageUrl(4))
    )
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
            )
        )
    )
}

private fun previewImageUrl(id: Int) =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
