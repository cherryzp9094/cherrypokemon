package com.cherryzp.cherrypokemon.ui.view.base.base

import androidx.compose.runtime.Immutable

@Immutable
abstract class UiState

interface UiSideEffect

sealed class BaseUiSideEffect : UiSideEffect {
    object OnClickBack : UiSideEffect
}