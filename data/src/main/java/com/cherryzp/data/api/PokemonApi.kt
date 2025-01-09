package com.cherryzp.data.api

import com.cherryzp.consts.KeyConsts.POKE_NO
import com.cherryzp.data.model.PokemonDetailResponse
import com.cherryzp.data.model.PokemonListResponse
import com.cherryzp.data.model.PokemonSpeciesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokemonApi {

    @GET("pokemon")
    suspend fun fetchPokemonList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): PokemonListResponse

    @GET("pokemon/{${POKE_NO}}")
    suspend fun fetchPokemonDetail(
        @Path(POKE_NO) pokeNo: Int
    ): PokemonDetailResponse

    @GET("pokemon-species/{${POKE_NO}}")
    suspend fun fetchPokemonSpecies(
        @Path(POKE_NO) pokeNo: Int
    ): PokemonSpeciesResponse
}