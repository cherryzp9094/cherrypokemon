package com.cherryzp.cherrypokemon.core.model

/**
 * 타입과 그 언어별 이름.
 *
 * 타입 자체는 색을 고르는 데 쓰고, 화면에 적는 글자는 [name] 에서 가져온다.
 * 아직 이름을 받지 못했으면 [name] 이 비어 있다.
 */
data class PokemonTypeInfo(val type: PokemonType, val name: LocalizedText = LocalizedText.Empty)
