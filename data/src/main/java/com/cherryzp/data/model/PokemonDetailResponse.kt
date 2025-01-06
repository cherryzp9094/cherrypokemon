package com.cherryzp.data.model

import com.google.gson.annotations.SerializedName

data class PokemonDetailResponse(
    val id: Int?,
    val name: String?,
    val base_experience: Int?,
    val height: Float?,
    val is_default: Boolean?,
    val order: Int?,
    val weight: Float?,
    val forms: List<Form>?,
    val species: Species?,
    val sprites: Sprites?,
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

    data class Species(
        val name: String,
        val url: String
    )

    data class Sprites(
        val back_default: String?,
        val back_shiny: String?,
        val front_default: String?,
        val front_shiny: String?,
        val other: Other?
    )

    data class Type(
        val slot: Int,
        val type: NamedAPIResource
    )

    data class Other(
        @SerializedName("official-artwork")
        val officialArtwork: OfficialArtwork?,
        val showdown: Showdown?
    )

    data class OfficialArtwork(
        val front_default: String?,
        val front_shiny: String?
    )

    data class Showdown(
        val back_default: String?,
        val back_female: String?,
        val back_shiny: String?,
        val back_shiny_female: String?,
        val front_default: String?,
        val front_female: String?,
        val front_shiny: String?,
        val front_shiny_female: String?
    )
}
