package com.cherryzp.cherrypokemon.core.model

/** 포켓몬의 종족값. 종류와 순서가 고정이라 목록이 아니라 필드로 담는다. */
data class PokemonStats(
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val specialAttack: Int,
    val specialDefense: Int,
    val speed: Int,
) {
    val total: Int get() = hp + attack + defense + specialAttack + specialDefense + speed

    companion object {
        /** 화면에서 막대 길이를 정할 때 쓰는 상한. 가장 높은 종족값(블리시)보다 크다. */
        const val MAX_VALUE = 255
    }
}
