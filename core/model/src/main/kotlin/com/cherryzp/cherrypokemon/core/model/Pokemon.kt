package com.cherryzp.cherrypokemon.core.model

/**
 * 목록에 보여줄 포켓몬.
 *
 * @param id 도감 번호
 * @param name PokeAPI 식별자(`bulbasaur`). 이름을 아직 받지 못했을 때 대신 보여준다
 * @param imageUrl 공식 일러스트 주소
 * @param localizedName 언어별 이름(이상해씨 / Bulbasaur). 아직 받지 못했으면 비어 있다
 */
data class Pokemon(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val localizedName: LocalizedText = LocalizedText.Empty,
)
