package com.cherryzp.cherrypokemon.core.data.model

import com.cherryzp.cherrypokemon.core.database.model.AbilityNameEntity
import com.cherryzp.cherrypokemon.core.database.model.TypeNameEntity
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.network.model.NetworkNamedEntry

internal fun NetworkNamedEntry.asTypeNameEntity(type: PokemonType) = TypeNameEntity(
    type = type.name,
    korean = korean(),
    english = english()
)

internal fun NetworkNamedEntry.asAbilityNameEntity() = AbilityNameEntity(
    name = name,
    korean = korean(),
    english = english()
)

private fun NetworkNamedEntry.korean() =
    names.firstOrNull { it.language.name == "ko" }?.name.orEmpty()

private fun NetworkNamedEntry.english() =
    names.firstOrNull { it.language.name == "en" }?.name.orEmpty()
