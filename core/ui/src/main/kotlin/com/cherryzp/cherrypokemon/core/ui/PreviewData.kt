package com.cherryzp.cherrypokemon.core.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.cherryzp.cherrypokemon.core.model.Pokemon
import com.cherryzp.cherrypokemon.core.model.PokemonAbility
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.model.PokemonStats
import com.cherryzp.cherrypokemon.core.model.PokemonType

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
            imageUrl = previewImageUrl(1),
            heightMeters = 0.7f,
            weightKilograms = 6.9f,
            types = listOf(PokemonType.Grass, PokemonType.Poison),
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
                PokemonAbility(name = "overgrow", isHidden = false),
                PokemonAbility(name = "chlorophyll", isHidden = true)
            )
        )
    )
}

private fun previewImageUrl(id: Int) =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
