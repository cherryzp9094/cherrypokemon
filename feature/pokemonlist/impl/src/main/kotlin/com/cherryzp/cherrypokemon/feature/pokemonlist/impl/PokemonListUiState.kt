package com.cherryzp.cherrypokemon.feature.pokemonlist.impl

/**
 * 목록 화면 상태.
 *
 * 로딩·에러·빈 상태는 Paging 의 `loadState` 가 들고 있어서 지금은 담을 값이 없다.
 * 화면에 상태가 생기면 `data class` 로 바꿔 필드를 넣는다.
 */
data object PokemonListUiState

/** 아직 보낼 일회성 이벤트가 없다. */
sealed interface PokemonListSideEffect
