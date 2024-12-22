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
    val stats: List<Stat>,
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

    data class VersionGroupDetail(
        val levelLearnedAt: Int,
        val moveLearnMethod: NamedAPIResource,
        val versionGroup: NamedAPIResource
    )

    data class Species(
        val name: String,
        val url: String
    )

    data class Sprites(
        val backDefault: String?,
        val backShiny: String?,
        val frontDefault: String?,
        val frontShiny: String?
    )

    data class Stat(
        val baseStat: Int,
        val effort: Int,
        val stat: NamedAPIResource
    )

    data class Type(
        val slot: Int,
        val type: NamedAPIResource
    )
}