package com.cherryzp.cherrypokemon.ui.view.base.base

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface Container<S : UiState> {
    val uiState: StateFlow<S>
    val uiSideEffect: SharedFlow<UiSideEffect>

    fun event(intent: ContainerContext<S>.() -> Unit)
}

interface ContainerHost<S : UiState> {
    val container: Container<S>
}

class ContainerContext<S : UiState>(
    val initState: () -> S,
    val postSideEffect: suspend (UiSideEffect) -> Unit,
    val reduceState: ((S) -> S) -> Unit
) {
    val state: S
        get() = initState()

}

fun <S: UiState> ContainerHost<S>.event(
    transformer: ContainerContext<S>.() -> Unit
) {
    container.event {
        transformer()
    }
}

fun <STATE : UiState> ContainerHost<STATE>.observe(
    lifecycleOwner: LifecycleOwner,
    sideEffect: ((sideEffect: UiSideEffect) -> Unit)? = null
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            sideEffect?.let { launch { container.uiSideEffect.collect { sideEffect(it) } } }
        }
    }
}

