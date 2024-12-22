package com.cherryzp.domain.model

data class PokemonDetail(
    val id: Int,
    val name: String,
    val baseExperience: Int,
    val height: Int,
    val isDefault: Boolean,
    val order: Int,
    val weight: Int,
    val forms: List<Form>,
    val species: Species,
    val sprites: Sprites,
    val types: List<Type>
) {
    data class Form(
        val name: String,
        val url: String
    )

    data class NamedAPIResource(
        val name: String,
        val url: String
    )

    data class Species(
        val name: String,
        val url: String
    )

    data class Sprites(
        val backDefault: String?,
        val backShiny: String?,
        val frontDefault: String?,
        val frontShiny: String?,
        val other: Other?
    )

    data class Type(
        val slot: Int,
        val type: NamedAPIResource
    )

    data class Other(
        val officialArtwork: OfficialArtwork?,
        val showdown: Showdown?
    )

    data class OfficialArtwork(
        val frontDefault: String?,
        val frontShiny: String?
    )

    data class Showdown(
        val backDefault: String?,
        val backFemale: String?,
        val backShiny: String?,
        val backShinyFemale: String?,
        val frontDefault: String?,
        val frontFemale: String?,
        val frontShiny: String?,
        val frontShinyFemale: String?
    )
}