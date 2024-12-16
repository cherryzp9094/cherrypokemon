package com.cherryzp.data.mapper

import com.cherryzp.data.model.PokemonResponse
import com.cherryzp.domain.model.Pokemon

fun PokemonResponse.toDomain() = Pokemon(
    name = name.orEmpty(),
    url = url.orEmpty()
)