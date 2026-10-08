package com.cherryzp.cherrypokemon.core.testing.repository

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.cherryzp.cherrypokemon.core.data.repository.PokemonRepository
import com.cherryzp.cherrypokemon.core.model.Pokemon
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * ViewModel 테스트와 Hilt 계측 테스트에서 쓰는 [PokemonRepository] 대역.
 * 값은 `send…` 로 넣고, 실패는 `…Error` 로 지정한다.
 */
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
