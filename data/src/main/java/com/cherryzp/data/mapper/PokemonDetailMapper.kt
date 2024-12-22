package com.cherryzp.data.mapper

import com.cherryzp.data.extend.default
import com.cherryzp.data.model.PokemonDetailResponse
import com.cherryzp.domain.model.PokemonDetail

fun PokemonDetailResponse.toDomain() = PokemonDetail(
    id = id.default(),
    name = name.orEmpty(),
    baseExperience = base_experience.default(),
    height = height.default(),
    isDefault = is_default.default(),
    order = order.default(),
    weight = weight.default(),
    forms = forms.orEmpty().map { it.toDomain() },
    species = species.toDomain(),
    sprites = sprites.toDomain(),
    types = types.orEmpty().map { it.toDomain() }
)

fun PokemonDetailResponse.Form?.toDomain() = PokemonDetail.Form(
    name = this?.name.orEmpty(),
    url = this?.url.orEmpty()
)

fun PokemonDetailResponse.NamedAPIResource?.toDomain() = PokemonDetail.NamedAPIResource(
    name = this?.name.orEmpty(),
    url = this?.url.orEmpty()
)

fun PokemonDetailResponse.Species?.toDomain() = PokemonDetail.Species(
    name = this?.name.orEmpty(),
    url = this?.url.orEmpty()
)

fun PokemonDetailResponse.Sprites?.toDomain() = PokemonDetail.Sprites(
    backDefault = this?.back_default,
    backShiny = this?.back_shiny,
    frontDefault = this?.front_default,
    frontShiny = this?.front_shiny,
    other = this?.other.toDomain()
)

fun PokemonDetailResponse.Type?.toDomain() = PokemonDetail.Type(
    slot = this?.slot.default(),
    type = this?.type.toDomain()
)

fun PokemonDetailResponse.Other?.toDomain() = PokemonDetail.Other(
    officialArtwork = this?.officialArtwork.toDomain(),
    showdown = this?.showdown.toDomain()
)

fun PokemonDetailResponse.OfficialArtwork?.toDomain() = PokemonDetail.OfficialArtwork(
    frontDefault = this?.front_default,
    frontShiny = this?.front_shiny
)

fun PokemonDetailResponse.Showdown?.toDomain() = PokemonDetail.Showdown(
    backDefault = this?.back_default,
    backFemale = this?.back_female,
    backShiny = this?.back_shiny,
    backShinyFemale = this?.back_shiny_female,
    frontDefault = this?.front_default,
    frontFemale = this?.front_female,
    frontShiny = this?.front_shiny,
    frontShinyFemale = this?.front_shiny_female
)