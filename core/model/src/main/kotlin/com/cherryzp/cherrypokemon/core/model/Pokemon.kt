package com.cherryzp.cherrypokemon.core.model

/**
 * 목록에 보여줄 포켓몬.
 *
 * @param id 도감 번호
 * @param imageUrl 공식 일러스트 주소
 */
data class Pokemon(val id: Int, val name: String, val imageUrl: String)
