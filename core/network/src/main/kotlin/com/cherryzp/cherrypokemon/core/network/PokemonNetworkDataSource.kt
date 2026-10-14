package com.cherryzp.cherrypokemon.core.network

import com.cherryzp.cherrypokemon.core.network.model.NetworkEvolutionChain
import com.cherryzp.cherrypokemon.core.network.model.NetworkNamedEntry
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonDetail
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

    /** 특성의 언어별 이름. */
    suspend fun getAbility(name: String): NetworkNamedEntry

    /** 진화 체인. 종 정보의 `evolution_chain` 주소에서 ID 를 꺼내 부른다. */
    suspend fun getEvolutionChain(chainId: Int): NetworkEvolutionChain
}
