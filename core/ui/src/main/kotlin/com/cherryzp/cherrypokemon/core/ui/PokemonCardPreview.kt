package com.cherryzp.cherrypokemon.core.ui

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.cherryzp.cherrypokemon.core.designsystem.theme.CherryPokemonTheme
import com.cherryzp.cherrypokemon.core.model.Pokemon

@PreviewLightDark
@Composable
private fun PokemonCardPreview(
    @PreviewParameter(PokemonPreviewParameterProvider::class) pokemon: Pokemon,
) {
    CherryPokemonTheme {
        PokemonCard(
            name = pokemon.name,
            number = pokemon.id,
            imageUrl = pokemon.imageUrl,
            onClick = {},
            modifier = Modifier.width(160.dp)
        )
    }
}
