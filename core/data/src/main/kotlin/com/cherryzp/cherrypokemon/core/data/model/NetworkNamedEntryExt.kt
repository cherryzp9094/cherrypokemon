package com.cherryzp.cherrypokemon.core.data.model

import com.cherryzp.cherrypokemon.core.database.model.AbilityNameEntity
import com.cherryzp.cherrypokemon.core.database.model.TypeNameEntity
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.network.model.NetworkNamedEntry
import com.cherryzp.cherrypokemon.core.network.model.NetworkNamedResource
import com.cherryzp.cherrypokemon.core.network.model.NetworkSpeciesName
import com.cherryzp.cherrypokemon.core.network.model.NetworkType

internal fun NetworkType.asEntity(type: PokemonType) = TypeNameEntity(
    type = type.name,
    korean = names.korean(),
    english = names.english(),
    doubleDamageFrom = damageRelations.doubleDamageFrom.toTypes(),
    halfDamageFrom = damageRelations.halfDamageFrom.toTypes(),
    noDamageFrom = damageRelations.noDamageFrom.toTypes()
)

internal fun NetworkNamedEntry.asEntity() = AbilityNameEntity(
    name = name,
    korean = names.korean(),
    english = names.english()
)

private fun List<NetworkSpeciesName>.korean() =
    firstOrNull { it.language.name == "ko" }?.name.orEmpty()

private fun List<NetworkSpeciesName>.english() =
    firstOrNull { it.language.name == "en" }?.name.orEmpty()

/** 모르는 타입 이름은 버린다. 배율 계산에 쓸 수 없다. */
private fun List<NetworkNamedResource>.toTypes(): List<PokemonType> = mapNotNull { resource ->
    PokemonType.entries.firstOrNull { it.name.equals(resource.name, ignoreCase = true) }
}
