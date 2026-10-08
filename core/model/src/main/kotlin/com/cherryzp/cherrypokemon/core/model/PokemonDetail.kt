package com.cherryzp.cherrypokemon.core.model

/**
 * 상세 화면에 보여줄 포켓몬.
 *
 * @param heightMeters 키(m)
 * @param weightKilograms 몸무게(kg)
 */
data class PokemonDetail(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val heightMeters: Float,
    val weightKilograms: Float,
    val types: List<PokemonType>,
)
