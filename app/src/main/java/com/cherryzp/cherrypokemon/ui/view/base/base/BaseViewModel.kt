package com.cherryzp.cherrypokemon.ui.view.base.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

abstract class BaseViewModel<S : UiState> : ViewModel(), ContainerHost<S> {

    abstract val initialState: S
    override val container: Container<S> by lazy {
        RealContainer(initialState)
    }

    protected fun reduceState(uiState: (uiState: S) -> S) = event {
        reduceState { uiState(it) }
    }

    protected fun <SEU> postSideEffect(
        postFunc: suspend (state: S) -> SEU,
    ) = event {
        viewModelScope.launch {
            val sideEffectOrUnit = postFunc(state)
            if (sideEffectOrUnit is UiSideEffect) {
                postSideEffect(sideEffectOrUnit)
            }
        }
    }
}