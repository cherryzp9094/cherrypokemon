package com.cherryzp.cherrypokemon.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `/evolution-chain/{id}` 응답. 1 단계부터 트리로 내려온다. */
@Serializable
data class NetworkEvolutionChain(val id: Int = 0, val chain: NetworkChainLink = NetworkChainLink())

@Serializable
data class NetworkChainLink(
    val species: NetworkNamedResource = NetworkNamedResource(),
    @SerialName("evolution_details")
    val evolutionDetails: List<NetworkEvolutionDetail> = emptyList(),
    @SerialName("evolves_to")
    val evolvesTo: List<NetworkChainLink> = emptyList(),
)

/**
 * 진화 조건. 게임 버전마다 여럿 올 수 있다. 어느 것을 쓸지는 데이터 레이어가 고른다.
 *
 * @param minLevel 레벨업 진화에 필요한 레벨
 * @param item 돌 같은 도구
 * @param heldItem 지니고 교환·레벨업해야 하는 도구(야도킹의 왕의징표석)
 * @param timeOfDay `day` / `night`. 상관없으면 빈 문자열
 * @param knownMove 배우고 있어야 하는 기술
 * @param knownMoveType 배우고 있어야 하는 기술의 타입(님피아의 페어리)
 * @param location 특정 장소에서 레벨업
 * @param evolvedPokemonForm 지역 폼으로 진화할 때만 온다. 기본 모습 체인에는 쓰지 않는다
 */
@Serializable
data class NetworkEvolutionDetail(
    val trigger: NetworkNamedResource? = null,
    @SerialName("min_level")
    val minLevel: Int? = null,
    val item: NetworkNamedResource? = null,
    @SerialName("held_item")
    val heldItem: NetworkNamedResource? = null,
    @SerialName("min_happiness")
    val minHappiness: Int? = null,
    @SerialName("min_affection")
    val minAffection: Int? = null,
    @SerialName("time_of_day")
    val timeOfDay: String = "",
    @SerialName("known_move")
    val knownMove: NetworkNamedResource? = null,
    @SerialName("known_move_type")
    val knownMoveType: NetworkNamedResource? = null,
    val location: NetworkNamedResource? = null,
    @SerialName("evolved_pokemon_form")
    val evolvedPokemonForm: NetworkNamedResource? = null,
)
