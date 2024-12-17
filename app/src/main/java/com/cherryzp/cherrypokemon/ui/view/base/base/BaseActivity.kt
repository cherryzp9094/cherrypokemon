package com.cherryzp.cherrypokemon.ui.view.base.base

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cherryzp.cherrypokemon.ui.theme.CherryPokemonTheme

abstract class BaseActivity<V : BaseViewModel<S>, S : UiState>: ComponentActivity() {

    abstract val viewModel: V

    @Composable
    abstract fun BuildContent(
        paddingValues: PaddingValues,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CherryPokemonTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    BuildContent(paddingValues)
                }
            }
        }
        viewModelObserve()
    }

    private fun viewModelObserve() {
        viewModel.observe(
            lifecycleOwner = this,
            sideEffect = ::handleSideEffect
        )
    }

    abstract fun handleSideEffect(sideEffect: UiSideEffect)
}