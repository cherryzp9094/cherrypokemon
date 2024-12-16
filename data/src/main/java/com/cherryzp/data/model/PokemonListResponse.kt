package com.cherryzp.data.model

data class PokemonListResponse(
    val count: Int?,
    val next: String?,
    val previous: String?,
    val results: List<PokemonResponse>?
)
