package com.cherryzp.domain.repository

import com.cherryzp.domain.model.Pokemon
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {
    suspend fun fetchPokemonList(
        limit: Int,
        offset: Int
    ): List<Pokemon>
}