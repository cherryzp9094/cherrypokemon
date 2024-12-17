package com.cherryzp.cherrypokemon.ui.view.base.base

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class RealContainer<S : UiState>(
    initialState: S
) : Container<S> {

    private val pluginContext: ContainerContext<S> = ContainerContext(
        initState = { uiState.value },
        postSideEffect = { internalUiSideEffect.emit(it) },
        reduceState = {
            internalUiStateFlow.update(it)
        }
    )
    private val internalUiStateFlow = MutableStateFlow(initialState)
    override val uiState: StateFlow<S> = internalUiStateFlow

    private val internalUiSideEffect = MutableSharedFlow<UiSideEffect>()
    override val uiSideEffect: SharedFlow<UiSideEffect> = internalUiSideEffect

    override fun event(intent: ContainerContext<S>.() -> Unit) {
        pluginContext.intent()
    }

}
