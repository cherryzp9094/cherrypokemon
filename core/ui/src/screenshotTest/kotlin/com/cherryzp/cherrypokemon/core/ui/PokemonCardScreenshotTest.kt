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

    /** 폼은 다섯 자리 번호다. 가장 좁은 카드(150dp)에서도 번호가 카드 안에 들어와야 한다. */
    @PreviewTest
    @PreviewLightDark
    @Composable
    fun pokemonCard_fiveDigitNumber() {
        CherryPokemonTheme {
            PokemonCard(
                name = "charizard-mega-x",
                number = 10034,
                imageUrl = "",
                onClick = {},
                modifier = Modifier.width(150.dp)
            )
        }
    }
}
