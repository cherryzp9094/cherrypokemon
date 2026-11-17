package com.cherryzp.cherrypokemon.core.model

/**
 * 같은 종의 한 모습(리자몽, 메가리자몽X, 거다이맥스 리자몽).
 *
 * @param id 도감 번호. 폼은 10001 번부터다
 * @param name PokeAPI 식별자. 이름을 아직 받지 못했을 때 대신 보여준다
 * @param localizedName 언어별 이름. 아직 받지 못했으면 비어 있다
 */
data class PokemonVariety(
    val id: Int,
    val name: String,
    val localizedName: LocalizedText,
    val imageUrl: String,
)
