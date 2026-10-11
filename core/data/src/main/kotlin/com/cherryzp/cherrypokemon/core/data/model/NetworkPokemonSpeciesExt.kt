package com.cherryzp.cherrypokemon.core.data.model

import com.cherryzp.cherrypokemon.core.database.model.LocalizedTextEntity
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonSpecies

private const val KOREAN = "ko"
private const val ENGLISH = "en"

internal fun NetworkPokemonSpecies.localizedName() = LocalizedTextEntity(
    korean = names.firstOrNull { it.language.name == KOREAN }?.name.orEmpty(),
    english = names.firstOrNull { it.language.name == ENGLISH }?.name.orEmpty()
)

internal fun NetworkPokemonSpecies.localizedGenus() = LocalizedTextEntity(
    korean = genera.firstOrNull { it.language.name == KOREAN }?.genus.orEmpty(),
    english = genera.firstOrNull { it.language.name == ENGLISH }?.genus.orEmpty()
)

/** 버전마다 하나씩 오므로 가장 최근 것만 쓴다. */
internal fun NetworkPokemonSpecies.localizedFlavorText() = LocalizedTextEntity(
    korean = flavorTextEntries.lastOrNull { it.language.name == KOREAN }?.flavorText.clean(),
    english = flavorTextEntries.lastOrNull { it.language.name == ENGLISH }?.flavorText.clean()
)

/**
 * 도감 설명에는 게임 화면 폭에 맞춘 줄바꿈과 폼피드가 섞여 있다.
 * 그대로 두면 아무 데서나 줄이 끊기므로 공백으로 바꾼다.
 */
private fun String?.clean(): String =
    this?.replace('\n', ' ')?.replace('\u000c', ' ')?.replace(Regex(" +"), " ")?.trim().orEmpty()
