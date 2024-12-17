package com.cherryzp.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.cherryzp.data.api.PokemonApi
import com.cherryzp.data.paging.PokemonPagingSource
import com.cherryzp.domain.model.Pokemon
import com.cherryzp.domain.repository.PokemonRepository
import kotlinx.coroutines.flow.Flow
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
}