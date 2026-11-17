package com.cherryzp.cherrypokemon.core.network

import com.cherryzp.cherrypokemon.core.network.model.NetworkAbility
import com.cherryzp.cherrypokemon.core.network.model.NetworkEvolutionChain
import com.cherryzp.cherrypokemon.core.network.model.NetworkMoveDetail
import com.cherryzp.cherrypokemon.core.network.model.NetworkNamedEntry
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonDetail
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonFilters
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonMoves
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonNames
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonPage
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonSpecies
import com.cherryzp.cherrypokemon.core.network.model.NetworkType

/** PokeAPI 호출. 실패하면 예외를 그대로 던진다. */
interface PokemonNetworkDataSource {
    suspend fun getPokemons(offset: Int, limit: Int): NetworkPokemonPage

    suspend fun getPokemonDetail(pokeId: Int): NetworkPokemonDetail

    /** 언어별 이름·분류·도감 설명. 상세와 함께 받아 로컬에 같이 쓴다. */
    suspend fun getPokemonSpecies(pokeId: Int): NetworkPokemonSpecies

    /** 타입의 언어별 이름. 타입은 20여 개뿐이라 한 번 받으면 재사용한다. */
    suspend fun getType(name: String): NetworkType

    /** 특성의 언어별 이름과 설명. */
    suspend fun getAbility(name: String): NetworkAbility

    /** 진화 체인. 종 정보의 `evolution_chain` 주소에서 ID 를 꺼내 부른다. */
    suspend fun getEvolutionChain(chainId: Int): NetworkEvolutionChain

    /** 알 그룹의 언어별 이름. 15 개뿐이다. */
    suspend fun getEggGroup(name: String): NetworkNamedEntry

    /** 진화 도구 같은 아이템의 언어별 이름. */
    suspend fun getItem(name: String): NetworkNamedEntry

    /** 기술 하나의 언어별 이름과 정보. */
    suspend fun getMove(name: String): NetworkMoveDetail

    /** 레벨업으로 배우는 기술과 그 기술 정보. 본편 게임 것만 GraphQL 한 번으로 받는다. */
    suspend fun getPokemonMoves(pokeId: Int): NetworkPokemonMoves

    /** 모든 포켓몬의 타입과 세대. GraphQL 한 번으로 받는다. */
    suspend fun getPokemonFilters(): NetworkPokemonFilters

    /** 모든 종과 폼의 한국어·영어 이름. GraphQL 한 번으로 받는다. */
    suspend fun getPokemonNames(): NetworkPokemonNames
}
