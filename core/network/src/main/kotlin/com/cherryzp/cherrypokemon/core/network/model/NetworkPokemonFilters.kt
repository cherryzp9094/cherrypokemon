package com.cherryzp.cherrypokemon.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 모든 포켓몬의 타입과 세대. 목록을 타입·세대로 거를 때 쓴다. GraphQL 한 번으로 받는다. */
@Serializable
data class NetworkPokemonFiltersResponse(val data: NetworkPokemonFilters? = null)

@Serializable
data class NetworkPokemonFilters(
    @SerialName("pokemon") val pokemons: List<NetworkPokemonFilter> = emptyList(),
)

@Serializable
data class NetworkPokemonFilter(
    val id: Int = 0,
    @SerialName("pokemontypes") val types: List<NetworkPokemonTypeSlotName> = emptyList(),
    /** 종. 세대가 들어 있다. */
    @SerialName("pokemonspecy") val species: NetworkFilterSpecies? = null,
)

@Serializable
data class NetworkPokemonTypeSlotName(
    val slot: Int = 0,
    val type: NetworkGraphQlName = NetworkGraphQlName(),
)

@Serializable
data class NetworkFilterSpecies(@SerialName("generation_id") val generation: Int? = null)
