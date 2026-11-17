package com.cherryzp.cherrypokemon.core.model

/**
 * 목록을 거르는 조건. 둘 다 null 이면 거르지 않는다.
 *
 * @param type 이 타입을 가진 포켓몬만(두 번째 타입도 본다)
 * @param generation 이 세대에 처음 나온 종만(1~9). 폼은 원래 종의 세대를 따른다
 */
data class PokemonFilter(val type: PokemonType? = null, val generation: Int? = null) {
    val isActive: Boolean get() = type != null || generation != null
}
