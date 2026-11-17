package com.cherryzp.cherrypokemon.core.data.model

import com.cherryzp.cherrypokemon.core.database.model.AbilityNameEntity
import com.cherryzp.cherrypokemon.core.database.model.TypeNameEntity
import com.cherryzp.cherrypokemon.core.model.PokemonType
import com.cherryzp.cherrypokemon.core.network.model.NetworkAbility
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

/** 설명은 게임마다 하나씩 오고 뒤로 갈수록 최신이라 언어마다 마지막 것을 쓴다. */
internal fun NetworkAbility.asEntity() = AbilityNameEntity(
    name = name,
    korean = names.korean(),
    english = names.english(),
    descriptionKorean = flavorTextEntries.lastOrNull { it.language.name == "ko" }
        ?.flavorText
        .clean(),
    descriptionEnglish = flavorTextEntries.lastOrNull { it.language.name == "en" }
        ?.flavorText
        .clean()
)

private fun List<NetworkSpeciesName>.korean() =
    firstOrNull { it.language.name == "ko" }?.name.orEmpty()

private fun List<NetworkSpeciesName>.english() =
    firstOrNull { it.language.name == "en" }?.name.orEmpty()

/** 모르는 타입 이름은 버린다. 배율 계산에 쓸 수 없다. */
private fun List<NetworkNamedResource>.toTypes(): List<PokemonType> = mapNotNull { resource ->
    PokemonType.entries.firstOrNull { it.name.equals(resource.name, ignoreCase = true) }
}
