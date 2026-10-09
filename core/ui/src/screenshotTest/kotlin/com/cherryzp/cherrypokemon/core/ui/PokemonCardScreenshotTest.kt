package com.cherryzp.cherrypokemon.core.ui

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.cherryzp.cherrypokemon.core.designsystem.theme.CherryPokemonTheme

/** 카드 모양이 바뀌면 참조 이미지와 달라져 실패한다. */
class PokemonCardScreenshotTest {

    @PreviewTest
    @PreviewLightDark
    @Composable
    fun pokemonCard() {
        CherryPokemonTheme {
            PokemonCard(
                name = "bulbasaur",
                number = 1,
                imageUrl = "",
                onClick = {},
                modifier = Modifier.width(160.dp)
            )
        }
    }
}
