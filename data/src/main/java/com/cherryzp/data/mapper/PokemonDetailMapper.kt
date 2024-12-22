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

fun PokemonDetailResponse.VersionGroupDetail?.toDomain() = PokemonDetail.VersionGroupDetail(
    levelLearnedAt = this?.level_learned_at.default(),
    moveLearnMethod = this?.move_learn_method.toDomain(),
    versionGroup = this?.version_group.toDomain()
)

fun PokemonDetailResponse.Species?.toDomain() = PokemonDetail.Species(
    name = this?.name.orEmpty(),
    url = this?.url.orEmpty()
)

fun PokemonDetailResponse.Sprites?.toDomain() = PokemonDetail.Sprites(
    backDefault = this?.back_default.orEmpty(),
    backShiny = this?.back_shiny.orEmpty(),
    frontDefault = this?.front_default.orEmpty(),
    frontShiny = this?.front_shiny.orEmpty()
)

fun PokemonDetailResponse.Type?.toDomain() = PokemonDetail.Type(
    slot = this?.slot.default(),
    type = this?.type.toDomain()
)