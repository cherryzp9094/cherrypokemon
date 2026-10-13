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
 * 진화 조건. 게임 버전마다 여럿 올 수 있어 첫 번째만 쓴다.
 *
 * @param minLevel 레벨업 진화에 필요한 레벨
 * @param item 돌 같은 도구
 */
@Serializable
data class NetworkEvolutionDetail(
    val trigger: NetworkNamedResource? = null,
    @SerialName("min_level")
    val minLevel: Int? = null,
    val item: NetworkNamedResource? = null,
)
