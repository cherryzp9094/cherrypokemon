package com.cherryzp.cherrypokemon.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 한 포켓몬이 레벨업으로 배우는 기술. GraphQL 한 번으로 기술 정보까지 함께 받는다. */
@Serializable
data class NetworkPokemonMovesResponse(val data: NetworkPokemonMoves? = null)

@Serializable
data class NetworkPokemonMoves(
    @SerialName("pokemonmove")
    val moves: List<NetworkPokemonMove> = emptyList(),
)

/**
 * 어느 게임에서 몇 레벨에 배우는지. 게임마다 따로 온다.
 *
 * @param level 0 이면 진화할 때 배운다
 */
@Serializable
data class NetworkPokemonMove(
    val level: Int = 0,
    @SerialName("versiongroup") val versionGroup: NetworkGraphQlName = NetworkGraphQlName(),
    val move: NetworkMove = NetworkMove(),
)

/**
 * 기술. 변화 기술은 위력이, 반드시 맞는 기술은 명중이 없다(null).
 *
 * @param damageClass `physical` / `special` / `status`
 */
@Serializable
data class NetworkMove(
    val name: String = "",
    val power: Int? = null,
    val accuracy: Int? = null,
    val pp: Int? = null,
    val type: NetworkGraphQlName? = null,
    @SerialName("movedamageclass") val damageClass: NetworkGraphQlName? = null,
    @SerialName("movenames") val names: List<NetworkGraphQlLanguageName> = emptyList(),
)

@Serializable
data class NetworkGraphQlName(val name: String = "")

/** 한 언어의 이름. 언어는 PokeAPI 언어 ID 다(한국어 3, 영어 9). */
@Serializable
data class NetworkGraphQlLanguageName(
    val name: String = "",
    @SerialName("language_id") val languageId: Int = 0,
)

/** `/move/{name}` 응답. 진화 조건에 나오는 기술처럼 하나만 필요할 때 쓴다. */
@Serializable
data class NetworkMoveDetail(
    val name: String = "",
    val power: Int? = null,
    val accuracy: Int? = null,
    val pp: Int? = null,
    val type: NetworkNamedResource? = null,
    @SerialName("damage_class") val damageClass: NetworkNamedResource? = null,
    val names: List<NetworkSpeciesName> = emptyList(),
)
