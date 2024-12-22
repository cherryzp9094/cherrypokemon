package com.cherryzp.data.model

import com.google.gson.annotations.SerializedName

data class PokemonDetailResponse(
    val id: Int?,
    val name: String?,
    val base_experience: Int?,
    val height: Int?,
    val is_default: Boolean?,
    val order: Int?,
    val weight: Int?,
    val forms: List<Form>?,
    val species: Species?,
    val sprites: Sprites?,
    val stats: List<Stat>?,
    val types: List<Type>?
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
        val level_learned_at: Int,
        val move_learn_method: NamedAPIResource,
        val version_group: NamedAPIResource
    )

    data class Species(
        val name: String,
        val url: String
    )

    data class Sprites(
        val back_default: String?,
        val back_shiny: String?,
        val front_default: String?,
        val front_shiny: String?
    )

    data class Stat(
        val base_stat: Int,
        val effort: Int,
        val stat: NamedAPIResource
    )

    data class Type(
        val slot: Int,
        val type: NamedAPIResource
    )
}
