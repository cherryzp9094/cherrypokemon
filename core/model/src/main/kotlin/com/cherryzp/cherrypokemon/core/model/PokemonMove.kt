package com.cherryzp.cherrypokemon.core.model

/**
 * 포켓몬이 레벨업으로 배우는 기술 하나.
 *
 * @param name PokeAPI 식별자(`thunder-shock`)
 * @param localizedName 언어별 이름(전기쇼크 / Thunder Shock)
 * @param power 위력. 변화 기술은 null
 * @param accuracy 명중. 반드시 맞는 기술은 null
 * @param damageClass `physical` / `special` / `status`
 * @param level 배우는 레벨. 0 이면 진화할 때 배운다
 */
data class PokemonMove(
    val name: String,
    val localizedName: LocalizedText,
    val type: PokemonTypeInfo,
    val power: Int?,
    val accuracy: Int?,
    val pp: Int?,
    val damageClass: String?,
    val level: Int,
)
