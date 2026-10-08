package com.cherryzp.cherrypokemon.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 상세 응답.
 *
 * @param height 데시미터 단위. 미터로 바꾸는 일은 데이터 레이어가 한다.
 * @param weight 헥토그램 단위.
 */
@Serializable
data class NetworkPokemonDetail(
    val id: Int = 0,
    val name: String = "",
    val height: Int = 0,
    val weight: Int = 0,
    val types: List<NetworkPokemonTypeSlot> = emptyList(),
    val sprites: NetworkSprites = NetworkSprites(),
)

@Serializable
data class NetworkPokemonTypeSlot(
    val slot: Int = 0,
    val type: NetworkNamedResource = NetworkNamedResource(),
)

@Serializable
data class NetworkSprites(val other: NetworkOtherSprites? = null)

@Serializable
data class NetworkOtherSprites(
    @SerialName("official-artwork")
    val officialArtwork: NetworkOfficialArtwork? = null,
)

@Serializable
data class NetworkOfficialArtwork(
    @SerialName("front_default")
    val frontDefault: String? = null,
)
