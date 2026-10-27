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
import org.orbitmvi.orbit.syntax.Syntax
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
                // 오프라인이면 갱신이 캐시보다 먼저 실패할 수 있다. 그때 refresh() 는 캐시가 없다고 보고
                // 메시지를 건너뛰므로, 실패한 뒤 캐시가 처음 도착하는 이 순간에 대신 알린다.
                val cacheArrivedAfterFailure = state.pokemonDetail == null &&
                    detail != null &&
                    state.refreshState == RefreshState.Failed
                reduce { state.copy(pokemonDetail = detail) }
                if (cacheArrivedAfterFailure) postRefreshFailedMessage()
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
            // 캐시가 아직 안 왔으면 observePokemonDetail() 이 도착할 때 띄운다.
            if (state.pokemonDetail != null) postRefreshFailedMessage()
        }
    }

    private suspend fun DetailSyntax.postRefreshFailedMessage() = postSideEffect(
        PokemonDetailSideEffect.ShowMessage(R.string.feature_pokemondetail_impl_error_refresh)
    )

    @AssistedFactory
    interface Factory {
        fun create(pokeId: Int): PokemonDetailViewModel
    }
}

private typealias DetailSyntax = Syntax<PokemonDetailUiState, PokemonDetailSideEffect>
