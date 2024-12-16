package com.cherryzp.domain.model

data class Pokemon(
    val name: String,
    val url: String,
) {
    val id: Int
        get() = url.split("/").lastOrNull { it.isNotEmpty() }?.toInt() ?: 0

    val imageUrl: String
        get() = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
}
