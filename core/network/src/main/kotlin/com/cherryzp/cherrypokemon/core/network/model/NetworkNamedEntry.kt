package com.cherryzp.cherrypokemon.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `/ability/{name}` 응답에서 쓰는 부분. 언어별 이름만 꺼낸다. */
@Serializable
data class NetworkNamedEntry(
    val id: Int = 0,
    val name: String = "",
    val names: List<NetworkSpeciesName> = emptyList(),
)

/** `/type/{name}` 응답. 언어별 이름과 받는 쪽 상성을 꺼낸다. */
@Serializable
data class NetworkType(
    val id: Int = 0,
    val name: String = "",
    val names: List<NetworkSpeciesName> = emptyList(),
    @SerialName("damage_relations")
    val damageRelations: NetworkDamageRelations = NetworkDamageRelations(),
)

/** 이 타입이 공격을 **받을 때**의 배율. 공격하는 쪽 상성은 쓰지 않는다. */
@Serializable
data class NetworkDamageRelations(
    @SerialName("double_damage_from")
    val doubleDamageFrom: List<NetworkNamedResource> = emptyList(),
    @SerialName("half_damage_from")
    val halfDamageFrom: List<NetworkNamedResource> = emptyList(),
    @SerialName("no_damage_from")
    val noDamageFrom: List<NetworkNamedResource> = emptyList(),
)
