package com.cherryzp.cherrypokemon.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `/pokemon-species/{id}` 응답에서 쓰는 부분. 언어별 이름·분류·도감 설명이 들어 있다. */
@Serializable
data class NetworkPokemonSpecies(
    val id: Int,
    val names: List<NetworkSpeciesName> = emptyList(),
    val genera: List<NetworkGenus> = emptyList(),
    @SerialName("flavor_text_entries")
    val flavorTextEntries: List<NetworkFlavorText> = emptyList(),
    /** 진화 체인 주소. 끝의 숫자가 체인 ID 다. */
    @SerialName("evolution_chain")
    val evolutionChain: NetworkNamedResource? = null,
)

@Serializable
data class NetworkSpeciesName(val name: String, val language: NetworkNamedResource)

@Serializable
data class NetworkGenus(val genus: String, val language: NetworkNamedResource)

@Serializable
data class NetworkFlavorText(
    @SerialName("flavor_text")
    val flavorText: String,
    val language: NetworkNamedResource,
)
