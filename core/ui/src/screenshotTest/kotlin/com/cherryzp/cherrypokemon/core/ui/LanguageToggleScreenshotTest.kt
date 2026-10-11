package com.cherryzp.cherrypokemon.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.cherryzp.cherrypokemon.core.designsystem.theme.CherryPokemonTheme
import com.cherryzp.cherrypokemon.core.model.AppLanguage

/** 고른 쪽에만 배경이 들어가는지 고정한다. */
class LanguageToggleScreenshotTest {

    @PreviewTest
    @PreviewLightDark
    @Composable
    fun languageToggle() {
        CherryPokemonTheme {
            Surface {
                Column(Modifier.width(140.dp).padding(12.dp)) {
                    LanguageToggle(language = AppLanguage.KOREAN, onLanguageClick = {})
                    LanguageToggle(language = AppLanguage.ENGLISH, onLanguageClick = {})
                }
            }
        }
    }
}
