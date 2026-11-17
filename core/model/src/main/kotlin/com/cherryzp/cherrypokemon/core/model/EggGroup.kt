package com.cherryzp.cherrypokemon.core.model

/**
 * 알 그룹. 같은 그룹끼리 알을 낳을 수 있다.
 *
 * @param name PokeAPI 식별자(`monster`)
 * @param localizedName 언어별 이름(괴수 / Monster). 아직 받지 못했으면 비어 있다
 */
data class EggGroup(val name: String, val localizedName: LocalizedText = LocalizedText.Empty)
