package com.cherryzp.cherrypokemon.feature.pokemondetail.impl

import androidx.annotation.StringRes
import com.cherryzp.cherrypokemon.core.model.PokemonDetail

/**
 * 상세 화면 상태.
 *
 * 캐시가 있으면 갱신이 실패해도 계속 보여준다.
 */
internal data class PokemonDetailUiState(
    val pokemonDetail: PokemonDetail? = null,
    val refreshState: RefreshState,
)

internal enum class RefreshState { Refreshing, Idle, Failed }

/** 캐시가 없고 갱신이 실패했을 때만 에러 화면을 보여준다. */
internal val PokemonDetailUiState.showsError: Boolean
    get() = pokemonDetail == null && refreshState == RefreshState.Failed

/** 캐시도 없고 아직 받아오는 중이면 로딩을 보여준다. */
internal val PokemonDetailUiState.showsLoading: Boolean
    get() = pokemonDetail == null && refreshState == RefreshState.Refreshing

internal sealed interface PokemonDetailSideEffect {
    /** 캐시를 보여주는 중에 갱신이 실패했을 때. */
    data class ShowMessage(@StringRes val messageId: Int) : PokemonDetailSideEffect
}
