package com.cherryzp.cherrypokemon.core.model

/**
 * 진화 체인의 한 단계.
 *
 * 분기(이브이)도 같은 목록에 담는다. [stage] 가 같으면 나란히 놓는다.
 *
 * @param stage 1 부터 시작하는 단계
 * @param condition 이전 단계에서 이 단계가 되는 조건. 1 단계는 null
 */
data class EvolutionStage(
    val pokemonId: Int,
    val name: String,
    val localizedName: LocalizedText,
    val imageUrl: String,
    val stage: Int,
    val condition: EvolutionCondition?,
)

/**
 * 진화 조건.
 *
 * @param minLevel 레벨업 진화에 필요한 레벨
 * @param item 돌 같은 도구의 PokeAPI 식별자
 * @param trigger 레벨업·교환 같은 계기
 */
data class EvolutionCondition(
    val minLevel: Int? = null,
    val item: String? = null,
    val trigger: String? = null,
)
