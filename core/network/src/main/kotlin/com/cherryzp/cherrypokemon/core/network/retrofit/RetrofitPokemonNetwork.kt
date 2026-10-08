package com.cherryzp.cherrypokemon.core.network.retrofit

import com.cherryzp.cherrypokemon.core.network.BuildConfig
import com.cherryzp.cherrypokemon.core.network.PokemonNetworkDataSource
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonDetail
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonPage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private interface RetrofitPokemonNetworkApi {
    @GET("pokemon")
    suspend fun getPokemons(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): NetworkPokemonPage

    @GET("pokemon/{id}")
    suspend fun getPokemonDetail(@Path("id") pokeId: Int): NetworkPokemonDetail
}

@Singleton
internal class RetrofitPokemonNetwork @Inject constructor(
    networkJson: Json,
    okhttpCallFactory: dagger.Lazy<Call.Factory>,
) : PokemonNetworkDataSource {

    private val networkApi = Retrofit.Builder()
        .baseUrl(BuildConfig.POKE_API_BASE_URL)
        // 메인 스레드에서 OkHttp 가 만들어지지 않도록 Lazy 로 받는다.
        .callFactory { okhttpCallFactory.get().newCall(it) }
        .addConverterFactory(networkJson.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(RetrofitPokemonNetworkApi::class.java)

    override suspend fun getPokemons(offset: Int, limit: Int): NetworkPokemonPage =
        networkApi.getPokemons(offset = offset, limit = limit)

    override suspend fun getPokemonDetail(pokeId: Int): NetworkPokemonDetail =
        networkApi.getPokemonDetail(pokeId = pokeId)
}
