package com.cherryzp.cherrypokemon.feature.pokemonlist.impl

import com.cherryzp.cherrypokemon.core.model.AppLanguage

/**
 * 목록 화면 상태.
 *
 * 목록 자체는 Paging 이 들고 있고, 로딩·에러·빈 상태는 `loadState` 가 들고 있다.
 * 여기에는 화면이 직접 다루는 값만 담는다.
 */
data class PokemonListUiState(
    /** 고른 적이 없으면 null. 화면이 기기 언어로 떨어뜨린다. */
    val language: AppLanguage? = null,
)

/** 아직 보낼 일회성 이벤트가 없다. */
sealed interface PokemonListSideEffect
