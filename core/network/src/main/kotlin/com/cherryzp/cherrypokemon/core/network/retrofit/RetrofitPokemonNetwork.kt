package com.cherryzp.cherrypokemon.core.network.retrofit

import com.cherryzp.cherrypokemon.core.network.BuildConfig
import com.cherryzp.cherrypokemon.core.network.PokemonNetworkDataSource
import com.cherryzp.cherrypokemon.core.network.model.NetworkAbility
import com.cherryzp.cherrypokemon.core.network.model.NetworkEvolutionChain
import com.cherryzp.cherrypokemon.core.network.model.NetworkGraphQlRequest
import com.cherryzp.cherrypokemon.core.network.model.NetworkMoveDetail
import com.cherryzp.cherrypokemon.core.network.model.NetworkNamedEntry
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonDetail
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonFilters
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonFiltersResponse
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonMoves
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonMovesResponse
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonNames
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonNamesResponse
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonPage
import com.cherryzp.cherrypokemon.core.network.model.NetworkPokemonSpecies
import com.cherryzp.cherrypokemon.core.network.model.NetworkType
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

private interface RetrofitPokemonNetworkApi {
    @GET("pokemon")
    suspend fun getPokemons(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): NetworkPokemonPage

    @GET("pokemon/{id}")
    suspend fun getPokemonDetail(@Path("id") pokeId: Int): NetworkPokemonDetail

    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpecies(@Path("id") pokeId: Int): NetworkPokemonSpecies

    @GET("type/{name}")
    suspend fun getType(@Path("name") name: String): NetworkType

    @GET("ability/{name}")
    suspend fun getAbility(@Path("name") name: String): NetworkAbility

    @GET("evolution-chain/{id}")
    suspend fun getEvolutionChain(@Path("id") chainId: Int): NetworkEvolutionChain

    @GET("egg-group/{name}")
    suspend fun getEggGroup(@Path("name") name: String): NetworkNamedEntry

    @GET("move/{name}")
    suspend fun getMove(@Path("name") name: String): NetworkMoveDetail

    @GET("item/{name}")
    suspend fun getItem(@Path("name") name: String): NetworkNamedEntry

    /** GraphQL 은 주소가 달라서 전체 주소를 넘긴다. */
    @POST
    suspend fun getPokemonNames(
        @Url url: String,
        @Body body: NetworkGraphQlRequest,
    ): NetworkPokemonNamesResponse

    @POST
    suspend fun getPokemonFilters(
        @Url url: String,
        @Body body: NetworkGraphQlRequest,
    ): NetworkPokemonFiltersResponse

    @POST
    suspend fun getPokemonMoves(
        @Url url: String,
        @Body body: NetworkGraphQlRequest,
    ): NetworkPokemonMovesResponse
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

    override suspend fun getPokemonSpecies(pokeId: Int): NetworkPokemonSpecies =
        networkApi.getPokemonSpecies(pokeId = pokeId)

    override suspend fun getType(name: String): NetworkType = networkApi.getType(name = name)

    override suspend fun getAbility(name: String): NetworkAbility =
        networkApi.getAbility(name = name)

    override suspend fun getEvolutionChain(chainId: Int): NetworkEvolutionChain =
        networkApi.getEvolutionChain(chainId = chainId)

    override suspend fun getItem(name: String): NetworkNamedEntry = networkApi.getItem(name = name)

    override suspend fun getMove(name: String): NetworkMoveDetail = networkApi.getMove(name = name)

    override suspend fun getEggGroup(name: String): NetworkNamedEntry =
        networkApi.getEggGroup(name = name)

    override suspend fun getPokemonNames(): NetworkPokemonNames = networkApi
        .getPokemonNames(
            url = BuildConfig.POKE_API_GRAPHQL_URL,
            body = NetworkGraphQlRequest(POKEMON_NAMES_QUERY)
        )
        .data
        // 질의 오류는 HTTP 200 에 errors 만 담아 온다. 다른 네트워크 실패처럼 다루게 예외로 바꾼다.
        ?: throw IOException("GraphQL 응답에 data 가 없다")

    override suspend fun getPokemonFilters(): NetworkPokemonFilters = networkApi
        .getPokemonFilters(
            url = BuildConfig.POKE_API_GRAPHQL_URL,
            body = NetworkGraphQlRequest(POKEMON_FILTERS_QUERY)
        )
        .data
        ?: throw IOException("GraphQL 응답에 data 가 없다")

    override suspend fun getPokemonMoves(pokeId: Int): NetworkPokemonMoves = networkApi
        .getPokemonMoves(
            url = BuildConfig.POKE_API_GRAPHQL_URL,
            body = NetworkGraphQlRequest(POKEMON_MOVES_QUERY, variables = mapOf("id" to pokeId))
        )
        .data
        ?: throw IOException("GraphQL 응답에 data 가 없다")

    private companion object {
        const val KOREAN = 3
        const val ENGLISH = 9

        /**
         * 본편 게임에서 레벨업으로 배우는 기술. 어느 게임 것을 쓸지는 데이터 레이어가 고른다.
         *
         * 버전 그룹 번호는 출시 순서가 아니다(28 은 옛 일본판 적·녹). 그래서 본편 게임 이름으로 거른다.
         */
        val POKEMON_MOVES_QUERY = """
            query moves(${'$'}id: Int!) {
              pokemonmove(where: {
                pokemon_id: {_eq: ${'$'}id},
                movelearnmethod: {name: {_eq: "level-up"}},
                versiongroup: {name: {_in: [$MAIN_VERSION_GROUPS]}}
              }) {
                level
                versiongroup { name }
                move {
                  name power accuracy pp
                  type { name }
                  movedamageclass { name }
                  movenames(where: {language_id: {_in: [$KOREAN, $ENGLISH]}}) { name language_id }
                }
              }
            }
        """.trimIndent()

        /** 모든 포켓몬(폼 포함)의 타입과 세대. 약 1350 마리, 160KB. */
        val POKEMON_FILTERS_QUERY = """
            {
              pokemon {
                id
                pokemontypes { slot type { name } }
                pokemonspecy { generation_id }
              }
            }
        """.trimIndent()

        /** 최신 본편부터. 데이터 레이어도 이 순서로 고른다. */
        const val MAIN_VERSION_GROUPS =
            "\"scarlet-violet\", \"brilliant-diamond-shining-pearl\", \"sword-shield\", " +
                "\"ultra-sun-ultra-moon\", \"sun-moon\", \"omega-ruby-alpha-sapphire\", " +
                "\"x-y\", \"black-2-white-2\", \"black-white\", \"heartgold-soulsilver\", " +
                "\"platinum\", \"diamond-pearl\", \"emerald\", \"firered-leafgreen\", " +
                "\"ruby-sapphire\", \"crystal\", \"gold-silver\", \"yellow\", \"red-blue\""

        /**
         * 종 이름은 1~1025 번, 폼은 10001 번부터. 폼은 기본 모양의 이름만 쓴다.
         * `pokemonform` 은 폼 낱말(`female` → `암컷의 모습`)이다. 한국어 이름이 빠진 폼에 붙인다.
         */
        val POKEMON_NAMES_QUERY = """
            {
              pokemonspeciesname(where: {language_id: {_in: [$KOREAN, $ENGLISH]}}) {
                name pokemon_species_id language_id
              }
              pokemon(where: {id: {_gt: 10000}}) {
                id name pokemon_species_id
                pokemonforms(where: {is_default: {_eq: true}}) {
                  pokemonformnames(where: {language_id: {_in: [$KOREAN, $ENGLISH]}}) {
                    pokemon_name name language_id
                  }
                }
              }
              pokemonform(where: {form_name: {_neq: ""}}) {
                form_name
                pokemonformnames(where: {language_id: {_eq: $KOREAN}}) {
                  name
                }
              }
            }
        """.trimIndent()
    }
}
