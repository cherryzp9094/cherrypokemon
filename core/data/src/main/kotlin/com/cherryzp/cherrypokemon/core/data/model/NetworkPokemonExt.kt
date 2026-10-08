package com.cherryzp.cherrypokemon.core.data.model

import com.cherryzp.cherrypokemon.core.database.model.PokemonDetailEntity
import com.cherryzp.cherrypokemon.core.database.model.PokemonEntity
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.network.model.NetworkNamedResource
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonDetail

/** 목록 응답의 `{ name, url }` 을 엔티티로 바꾼다. URL 끝의 도감 번호를 꺼낸다. */
fun NetworkNamedResource.asEntity(): PokemonEntity? {
    val id = url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: return null
    return PokemonEntity(
        id = id,
        name = name,
        imageUrl = officialArtworkUrl(id)
    )
}

fun NetworkPokemonDetail.asEntity() = PokemonDetailEntity(
    id = id,
    name = name,
    imageUrl = sprites.other?.officialArtwork?.frontDefault ?: officialArtworkUrl(id),
    // PokeAPI 는 키를 데시미터, 몸무게를 헥토그램으로 준다.
    heightMeters = height / 10f,
    weightKilograms = weight / 10f,
    types = types.sortedBy { it.slot }.map { it.type.name.asPokemonType() }
)

private fun String.asPokemonType(): PokemonType =
    PokemonType.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }
        ?: PokemonType.Unknown

private fun officialArtworkUrl(id: Int) =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
