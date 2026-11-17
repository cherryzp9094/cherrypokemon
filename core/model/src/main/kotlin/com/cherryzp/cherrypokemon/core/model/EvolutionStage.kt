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
 * 진화 조건. 여러 개가 함께 걸릴 수 있다(님피아: 페어리 기술 + 친밀도).
 *
 * @param minLevel 레벨업 진화에 필요한 레벨
 * @param item 돌 같은 도구의 PokeAPI 식별자
 * @param itemName 도구의 언어별 이름. 아직 받지 못했으면 비어 있다
 * @param trigger 레벨업·교환 같은 계기(`level-up`, `trade`, `use-item`)
 * @param heldItem 지니고 있어야 하는 도구의 식별자(야도킹의 왕의징표석)
 * @param heldItemName 그 도구의 언어별 이름
 * @param minHappiness 필요한 친밀도
 * @param minAffection 필요한 친밀도(6세대 이후 이름)
 * @param timeOfDay `day` / `night`
 * @param knownMove 배우고 있어야 하는 기술의 식별자
 * @param knownMoveType 배우고 있어야 하는 기술의 타입
 * @param knownMoveTypeName 그 타입의 언어별 이름
 * @param location 진화하는 장소의 식별자. 게임마다 달라 장소 이름은 보여주지 않는다
 */
data class EvolutionCondition(
    val minLevel: Int? = null,
    val item: String? = null,
    val itemName: LocalizedText = LocalizedText.Empty,
    val trigger: String? = null,
    val heldItem: String? = null,
    val heldItemName: LocalizedText = LocalizedText.Empty,
    val minHappiness: Int? = null,
    val minAffection: Int? = null,
    val timeOfDay: String? = null,
    val knownMove: String? = null,
    val knownMoveType: PokemonType? = null,
    val knownMoveTypeName: LocalizedText = LocalizedText.Empty,
    val location: String? = null,
)
