package com.cherryzp.cherrypokemon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cherryzp.cherrypokemon.core.designsystem.theme.CherryPokemonTheme
import com.cherryzp.cherrypokemon.ui.CherryPokemonApp
import dagger.hilt.android.AndroidEntryPoint

/** 앱의 유일한 Activity. 화면은 NavKey 와 entry 로 추가한다. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CherryPokemonTheme {
                CherryPokemonApp()
            }
        }
    }
}
