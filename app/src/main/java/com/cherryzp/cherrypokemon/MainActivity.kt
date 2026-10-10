package com.cherryzp.cherrypokemon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cherryzp.cherrypokemon.core.designsystem.theme.CherryPokemonTheme
import com.cherryzp.cherrypokemon.core.model.AppLanguage
import com.cherryzp.cherrypokemon.core.ui.ProvideAppLocale
import com.cherryzp.cherrypokemon.ui.CherryPokemonApp
import dagger.hilt.android.AndroidEntryPoint

/** 앱의 유일한 Activity. 화면은 NavKey 와 entry 로 추가한다. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val saved by viewModel.language.collectAsStateWithLifecycle()
            // 고른 적이 없으면 기기 언어를 쓴다.
            val deviceLanguage = AppLanguage.fromTag(
                LocalConfiguration.current.locales[0]?.toLanguageTag()
            )

            CherryPokemonTheme {
                ProvideAppLocale(language = saved ?: deviceLanguage) {
                    CherryPokemonApp()
                }
            }
        }
    }
}
