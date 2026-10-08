package com.cherryzp.cherrypokemon.core.network

import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonDetail
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonPage

/** PokeAPI 호출. 실패하면 예외를 그대로 던진다. */
interface PokemonNetworkDataSource {
    suspend fun getPokemons(offset: Int, limit: Int): NetworkPokemonPage

    suspend fun getPokemonDetail(pokeId: Int): NetworkPokemonDetail
}
