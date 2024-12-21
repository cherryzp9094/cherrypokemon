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
    abilities = abilities.orEmpty().map { it.toDomain() },
    forms = forms.orEmpty().map { it.toDomain() },
    gameIndices = game_indices.orEmpty().map { it.toDomain() },
    locationAreaEncounters = location_area_encounters.orEmpty(),
    moves = moves.orEmpty().map { it.toDomain() },
    species = species.toDomain(),
    sprites = sprites.toDomain(),
    stats = stats.orEmpty().map { it.toDomain() },
    types = types.orEmpty().map { it.toDomain() }
)

fun PokemonDetailResponse.Ability?.toDomain() = PokemonDetail.Ability(
    ability = this?.ability.toDomain(),
    isHidden = this?.is_hidden.default(),
    slot = this?.slot.default()
)

fun PokemonDetailResponse.Form?.toDomain() = PokemonDetail.Form(
    name = this?.name.orEmpty(),
    url = this?.url.orEmpty()
)

fun PokemonDetailResponse.GameIndex?.toDomain() = PokemonDetail.GameIndex(
    gameIndex = this?.game_index.default(),
    version = this?.version.toDomain()
)

fun PokemonDetailResponse.Move?.toDomain() = PokemonDetail.Move(
    move = this?.move.toDomain(),
    versionGroupDetails = this?.version_group_details.orEmpty().map { it.toDomain() }
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

fun PokemonDetailResponse.Stat?.toDomain() = PokemonDetail.Stat(
    baseStat = this?.base_stat.default(),
    effort = this?.effort.default(),
    stat = this?.stat.toDomain()
)

fun PokemonDetailResponse.Type?.toDomain() = PokemonDetail.Type(
    slot = this?.slot.default(),
    type = this?.type.toDomain()
)