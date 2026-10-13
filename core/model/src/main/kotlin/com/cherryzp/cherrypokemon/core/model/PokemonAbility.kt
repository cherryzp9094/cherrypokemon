package com.cherryzp.cherrypokemon.core.model

/**
 * 포켓몬의 특성.
 *
 * @param name PokeAPI 식별자(`overgrow`)
 * @param isHidden 숨겨진 특성인지
 * @param localizedName 언어별 이름. 아직 받지 못했으면 비어 있다
 */
data class PokemonAbility(
    val name: String,
    val isHidden: Boolean,
    val localizedName: LocalizedText = LocalizedText.Empty,
)
