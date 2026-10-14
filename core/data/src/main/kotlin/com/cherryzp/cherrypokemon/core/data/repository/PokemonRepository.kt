package com.cherryzp.cherrypokemon.core.data.repository

import androidx.paging.PagingData
import com.cherryzp.cherrypokemon.core.model.Pokemon
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import kotlinx.coroutines.flow.Flow

/** 포켓몬 데이터를 로컬 데이터베이스 기준으로 제공하고, 네트워크에서 받아 로컬을 갱신한다. */
interface PokemonRepository {
    fun getPokemonsStream(): Flow<PagingData<Pokemon>>

    /**
     * 이름이나 번호로 찾는다. 로컬만 본다.
     *
     * 검색은 스크롤해서 받아온 것만이 아니라 전체에서 찾아야 하므로,
     * 호출하기 전에 [ensurePokemonIndex] 로 색인을 채운다.
     */
    fun searchPokemonsStream(query: String): Flow<PagingData<Pokemon>>

    /** 전체 이름 목록을 한 번 받아 둔다. 이미 있으면 아무것도 하지 않는다. */
    suspend fun ensurePokemonIndex()

    fun getPokemonDetailStream(pokeId: Int): Flow<PokemonDetail?>

    /** 네트워크에서 받아 로컬에 쓴다. 실패하면 예외를 던진다. */
    suspend fun refreshPokemonDetail(pokeId: Int)
}
