package com.cherryzp.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.cherryzp.data.api.PokemonApi
import com.cherryzp.data.mapper.toDomain
import com.cherryzp.data.paging.PokemonPagingSource
import com.cherryzp.domain.model.Pokemon
import com.cherryzp.domain.model.PokemonDetail
import com.cherryzp.domain.repository.PokemonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PokemonRepositoryImpl @Inject constructor(
    private val pokemonApi: PokemonApi
): PokemonRepository {
    override suspend fun fetchPokemonList(): Flow<PagingData<Pokemon>> =
        Pager(config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false
        ),
            pagingSourceFactory = {
                PokemonPagingSource(
                    pokemonApi = pokemonApi
                )
            }
        ).flow

    override suspend fun fetchPokemonDetail(pokeNo: Int): PokemonDetail =
        pokemonApi.fetchPokemonDetail(pokeNo).toDomain()
}