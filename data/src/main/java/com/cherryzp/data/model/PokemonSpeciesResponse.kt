package com.cherryzp.data.model

import com.google.gson.annotations.SerializedName

data class PokemonSpeciesResponse(
    @SerializedName("base_happiness") val baseHappiness: Int,
    @SerializedName("capture_rate") val captureRate: Int,
    @SerializedName("color") val color: NamedResource,
    @SerializedName("egg_groups") val eggGroups: List<NamedResource>,
    @SerializedName("name") val name: String,
    @SerializedName("order") val order: Int,
    @SerializedName("habitat") val habitat: NamedResource?
)

data class NamedResource(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String
)