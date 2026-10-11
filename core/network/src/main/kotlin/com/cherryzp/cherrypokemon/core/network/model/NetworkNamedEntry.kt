package com.cherryzp.cherrypokemon.core.network.model

import kotlinx.serialization.Serializable

/** `/type/{name}` 과 `/ability/{name}` 응답에서 쓰는 부분. 언어별 이름만 꺼낸다. */
@Serializable
data class NetworkNamedEntry(
    val id: Int = 0,
    val name: String = "",
    val names: List<NetworkSpeciesName> = emptyList(),
)
