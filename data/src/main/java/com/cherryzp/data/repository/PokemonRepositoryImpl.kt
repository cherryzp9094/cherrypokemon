package com.cherryzp.data.repository

import com.cherryzp.data.api.PokemonApi
import com.cherryzp.data.extend.default
import com.cherryzp.data.mapper.toDomain
import com.cherryzp.domain.model.Pokemon
import com.cherryzp.domain.repository.PokemonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class PokemonRepositoryImpl @Inject constructor(
    private val pokemonApi: PokemonApi
): PokemonRepository {
    override suspend fun fetchPokemonList(
        limit: Int,
        offset: Int
    ): List<Pokemon> = pokemonApi
        .fetchPokemonList(limit, offset).results?.map { it.toDomain() }.default()

}