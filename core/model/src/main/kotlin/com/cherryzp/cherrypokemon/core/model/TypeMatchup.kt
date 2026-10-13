package com.cherryzp.cherrypokemon.core.model

/**
 * 이 포켓몬이 공격을 받을 때의 배율.
 *
 * 타입이 둘이면 각 타입의 배율을 곱한다. 불꽃/비행은 바위 공격을 4배로 받는다.
 *
 * @param multiplier 0, 0.25, 0.5, 2, 4 처럼 1 이 아닌 값만 담는다
 */
data class TypeMatchup(val type: PokemonTypeInfo, val multiplier: Float) {
    val isWeakness: Boolean get() = multiplier > 1f
    val isResistance: Boolean get() = multiplier in 0.001f..0.999f
    val isImmunity: Boolean get() = multiplier == 0f
}
