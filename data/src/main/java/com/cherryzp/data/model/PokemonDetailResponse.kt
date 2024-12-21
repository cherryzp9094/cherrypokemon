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
    val abilities: List<Ability>?,
    val forms: List<Form>?,
    val game_indices: List<GameIndex>?,
    val location_area_encounters: String?,
    val moves: List<Move>?,
    val species: Species?,
    val sprites: Sprites?,
    val stats: List<Stat>?,
    val types: List<Type>?
) {
    data class Ability(
        val ability: NamedAPIResource,
        val is_hidden: Boolean,
        val slot: Int
    )

    data class Form(
        val name: String,
        val url: String
    )

    data class GameIndex(
        val game_index: Int,
        val version: NamedAPIResource
    )

    data class Move(
        val move: NamedAPIResource,
        val version_group_details: List<VersionGroupDetail>
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
