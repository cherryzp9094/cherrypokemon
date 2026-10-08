package com.cherryzp.cherrypokemon.core.data.repository

import androidx.paging.PagingData
import com.cherryzp.cherrypokemon.core.model.Pokemon
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import kotlinx.coroutines.flow.Flow

/** 포켓몬 데이터를 로컬 데이터베이스 기준으로 제공하고, 네트워크에서 받아 로컬을 갱신한다. */
interface PokemonRepository {
    fun getPokemonsStream(): Flow<PagingData<Pokemon>>

    fun getPokemonDetailStream(pokeId: Int): Flow<PokemonDetail?>

    /** 네트워크에서 받아 로컬에 쓴다. 실패하면 예외를 던진다. */
    suspend fun refreshPokemonDetail(pokeId: Int)
}
