package com.cherryzp.cherrypokemon.core.network.model

import kotlinx.serialization.Serializable

/** PokeAPI 가 여러 곳에서 쓰는 `{ name, url }` 구조. */
@Serializable
data class NetworkNamedResource(val name: String = "", val url: String = "")

/** 목록 응답. */
@Serializable
data class NetworkPokemonPage(
    val count: Int = 0,
    val next: String? = null,
    val previous: String? = null,
    val results: List<NetworkNamedResource> = emptyList(),
)
