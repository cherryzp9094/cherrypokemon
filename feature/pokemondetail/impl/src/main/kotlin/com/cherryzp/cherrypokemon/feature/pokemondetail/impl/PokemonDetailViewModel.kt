package com.cherryzp.cherrypokemon.feature.pokemondetail.impl

import androidx.lifecycle.ViewModel
import com.cherryzp.cherrypokemon.core.data.repository.PokemonRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.viewmodel.orbitContainer

/**
 * 상세 화면 ViewModel.
 *
 * 읽기는 로컬 스트림을 구독하고, 네트워크는 로컬을 갱신하는 데만 쓴다.
 */
@HiltViewModel(assistedFactory = PokemonDetailViewModel.Factory::class)
internal class PokemonDetailViewModel @AssistedInject constructor(
    @Assisted private val pokeId: Int,
    private val pokemonRepository: PokemonRepository,
) : ViewModel(),
    OrbitContainerHost<PokemonDetailUiState, PokemonDetailUiState, PokemonDetailSideEffect> {

    override val container = orbitContainer<PokemonDetailUiState, PokemonDetailSideEffect>(
        // 곧 갱신을 시작하므로 초기 상태도 갱신 중이다.
        initialState = PokemonDetailUiState(refreshState = RefreshState.Refreshing)
    ) {
        coroutineScope {
            launch { observePokemonDetail() }
            launch { refresh() }
        }
    }

    fun retry() = intent { refresh() }

    @OptIn(OrbitExperimental::class)
    private suspend fun observePokemonDetail() = subIntent {
        repeatOnSubscription {
            pokemonRepository.getPokemonDetailStream(pokeId).collect { detail ->
                reduce { state.copy(pokemonDetail = detail) }
            }
        }
    }

    private suspend fun refresh() = subIntent {
        reduce { state.copy(refreshState = RefreshState.Refreshing) }
        try {
            pokemonRepository.refreshPokemonDetail(pokeId)
            reduce { state.copy(refreshState = RefreshState.Idle) }
        } catch (e: IOException) {
            reduce { state.copy(refreshState = RefreshState.Failed) }
            // 캐시를 보여주는 중이면 화면을 바꾸지 않고 메시지만 띄운다.
            if (state.pokemonDetail != null) {
                postSideEffect(
                    PokemonDetailSideEffect.ShowMessage(
                        R.string.feature_pokemondetail_impl_error_refresh
                    )
                )
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(pokeId: Int): PokemonDetailViewModel
    }
}
