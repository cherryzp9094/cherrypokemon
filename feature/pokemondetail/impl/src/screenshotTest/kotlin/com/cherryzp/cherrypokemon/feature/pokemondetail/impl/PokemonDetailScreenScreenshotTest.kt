package com.cherryzp.cherrypokemon.feature.pokemondetail.impl

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.cherryzp.cherrypokemon.core.designsystem.theme.CherryPokemonTheme
import com.cherryzp.cherrypokemon.core.ui.FormFactorPreviews
import com.cherryzp.cherrypokemon.core.ui.PokemonDetailPreviewParameterProvider

/**
 * 상세 화면이 폰·폴드·태블릿에서 어떻게 깔리는지.
 *
 * 머리말 높이가 화면 높이를 따라가므로 태블릿(가로)에서는 낮은 머리말, 폰에서는 상한 300dp 가 찍힌다.
 */
class PokemonDetailScreenScreenshotTest {

    @PreviewTest
    @FormFactorPreviews
    @Composable
    fun pokemonDetail() {
        val detail = PokemonDetailPreviewParameterProvider().values.first()
        CherryPokemonTheme {
            PokemonDetailScreen(
                uiState = PokemonDetailUiState(
                    pokemonDetail = detail,
                    refreshState = RefreshState.Idle
                ),
                onRetryClick = {},
                onBackClick = {}
            )
        }
    }
}
