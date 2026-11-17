package com.cherryzp.cherrypokemon.core.testing.repository

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.cherryzp.cherrypokemon.core.data.repository.PokemonRepository
import com.cherryzp.cherrypokemon.core.model.Pokemon
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.model.PokemonFilter
import com.cherryzp.cherrypokemon.core.model.PokemonTypeInfo
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * ViewModel 테스트와 Hilt 계측 테스트에서 쓰는 [PokemonRepository] 대역.
 * 값은 `send…` 로 넣고, 실패는 `…Error` 로 지정한다.
 */
@Singleton
class FakePokemonRepository @Inject constructor() : PokemonRepository {

    private val pokemons = MutableStateFlow(PagingData.empty<Pokemon>(loadedStates))
    private val pokemonDetails = MutableSharedFlow<PokemonDetail?>(replay = 1)

    /** `refreshPokemonDetail` 이 던질 예외. null 이면 성공한다. */
    var refreshError: Throwable? = null

    /** `refreshPokemonDetail` 이 몇 번 불렸는지. */
    var refreshCount: Int = 0
        private set

    fun sendPokemons(values: List<Pokemon>) {
        // 로드 상태를 함께 주지 않으면 asSnapshot 이 로딩이 끝나기를 기다리며 멈춘다.
        pokemons.value = PagingData.from(values, loadedStates)
    }

    fun sendPokemonDetail(detail: PokemonDetail?) {
        pokemonDetails.tryEmit(detail)
    }

    override fun getPokemonsStream(): Flow<PagingData<Pokemon>> = pokemons

    /** 마지막으로 검색한 조건. ViewModel 이 거름을 넘기는지 보는 데 쓴다. */
    var lastFilter: PokemonFilter? = null
        private set

    override fun searchPokemonsStream(
        query: String,
        filter: PokemonFilter,
    ): Flow<PagingData<Pokemon>> {
        lastFilter = filter
        return pokemons
    }

    /** `ensurePokemonFilters` 가 몇 번 불렸는지. */
    var filtersCount: Int = 0
        private set

    override suspend fun ensurePokemonFilters() {
        filtersCount++
    }

    private val types = MutableStateFlow(emptyList<PokemonTypeInfo>())

    fun sendTypes(values: List<PokemonTypeInfo>) {
        types.value = values
    }

    override fun getTypesStream(): Flow<List<PokemonTypeInfo>> = types

    /** 몇 번 불렸는지. 검색이 색인을 채우는지 본다. */
    var indexCount: Int = 0
        private set

    /** `ensurePokemonIndex` 가 던질 예외. null 이면 성공한다. */
    var indexError: Throwable? = null

    override suspend fun ensurePokemonIndex() {
        indexCount++
        indexError?.let { throw it }
    }

    /** `ensurePokemonNames` 가 몇 번 불렸는지. */
    var namesCount: Int = 0
        private set

    override suspend fun ensurePokemonNames() {
        namesCount++
    }

    private companion object {
        val loadedStates = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = true),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true)
        )
    }

    override fun getPokemonDetailStream(pokeId: Int): Flow<PokemonDetail?> = pokemonDetails

    override suspend fun refreshPokemonDetail(pokeId: Int) {
        refreshCount++
        refreshError?.let { throw it }
    }
}
