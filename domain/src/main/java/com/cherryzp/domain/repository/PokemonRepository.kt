package com.cherryzp.domain.repository

import androidx.paging.PagingData
import com.cherryzp.domain.model.Pokemon
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {
    suspend fun fetchPokemonList(): Flow<PagingData<Pokemon>>
}