package com.cherryzp.cherrypokemon.ui.view.pokemonDetail

import android.view.Window
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.cherryzp.domain.model.PokemonDetail
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun PokemonDetailScreen(
    paddingValues: PaddingValues,
    window: Window,
    pokemonDetail: PokemonDetail,
    pokemonBackgroundColor: Int
) {

    LaunchedEffect(pokemonBackgroundColor) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color(pokemonBackgroundColor).toArgb()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = pokemonDetail.name)
        Text(text = pokemonDetail.height.toString())
        Text(text = pokemonDetail.weight.toString())

        GlideImage(
            modifier = Modifier.size(100.dp),
            imageModel = { pokemonDetail.sprites.frontDefault }
        )

        GlideImage(
            modifier = Modifier.size(100.dp),
            imageModel = { pokemonDetail.sprites.frontShiny }
        )

        GlideImage(
            modifier = Modifier.size(100.dp),
            imageModel = { pokemonDetail.sprites.backDefault }
        )

        GlideImage(
            modifier = Modifier.size(100.dp),
            imageModel = { pokemonDetail.sprites.backShiny }
        )

        GlideImage(
            modifier = Modifier.size(100.dp),
            imageModel = { pokemonDetail.sprites.other?.officialArtwork?.frontDefault }
        )

        GlideImage(
            modifier = Modifier.size(100.dp),
            imageModel = { pokemonDetail.sprites.other?.officialArtwork?.frontShiny }
        )

        GlideImage(
            modifier = Modifier.size(100.dp),
            imageModel = { pokemonDetail.sprites.other?.showdown?.frontDefault }
        )

        GlideImage(
            modifier = Modifier.size(100.dp),
            imageModel = { pokemonDetail.sprites.other?.showdown?.backDefault }
        )

        Text(text = pokemonDetail.toString())
    }

}