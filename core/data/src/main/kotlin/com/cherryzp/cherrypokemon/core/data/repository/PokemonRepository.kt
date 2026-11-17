package com.cherryzp.cherrypokemon.core.data.repository

import androidx.paging.PagingData
import com.cherryzp.cherrypokemon.core.model.Pokemon
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.model.PokemonFilter
import com.cherryzp.cherrypokemon.core.model.PokemonTypeInfo
import kotlinx.coroutines.flow.Flow

/** 포켓몬 데이터를 로컬 데이터베이스 기준으로 제공하고, 네트워크에서 받아 로컬을 갱신한다. */
interface PokemonRepository {
    fun getPokemonsStream(): Flow<PagingData<Pokemon>>

    /**
     * 이름이나 번호로 찾고 [filter] 로 거른다. 로컬만 본다. 검색어가 비어 있으면 거르기만 한다.
     *
     * 검색은 스크롤해서 받아온 것만이 아니라 전체에서 찾아야 하므로,
     * 호출하기 전에 [ensurePokemonIndex] 로 색인을 채운다.
     */
    fun searchPokemonsStream(
        query: String,
        filter: PokemonFilter = PokemonFilter(),
    ): Flow<PagingData<Pokemon>>

    /**
     * 타입·세대로 거를 수 있게 모든 포켓몬의 타입과 세대, 18 타입의 이름을 한 번 받아 둔다.
     * 이미 있으면 받지 않는다.
     */
    suspend fun ensurePokemonFilters()

    /** 거름 단추에 쓸 타입과 그 언어별 이름. 이름을 아직 받지 못한 타입은 비어 있다. */
    fun getTypesStream(): Flow<List<PokemonTypeInfo>>

    /** 전체 이름 목록을 한 번 받아 둔다. 이미 있으면 아무것도 하지 않는다. */
    suspend fun ensurePokemonIndex()

    /**
     * 모든 포켓몬의 한국어·영어 이름을 한 번 받아 둔다. 이미 있으면 아무것도 하지 않는다.
     *
     * 목록 카드, 한국어 검색, 아직 열어보지 않은 진화 단계가 이 이름을 쓴다.
     */
    suspend fun ensurePokemonNames()

    fun getPokemonDetailStream(pokeId: Int): Flow<PokemonDetail?>

    /** 네트워크에서 받아 로컬에 쓴다. 실패하면 예외를 던진다. */
    suspend fun refreshPokemonDetail(pokeId: Int)
}
