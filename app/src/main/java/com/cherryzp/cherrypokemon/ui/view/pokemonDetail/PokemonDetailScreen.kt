package com.cherryzp.cherrypokemon.ui.view.pokemonDetail

import android.view.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .verticalScroll(rememberScrollState())
    ) {

        GlideImage(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp
                    )
                )
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(pokemonBackgroundColor)),
            imageModel = { pokemonDetail.sprites.other?.officialArtwork?.frontDefault }
        )

        Text(
            modifier = Modifier
                .padding(top = 24.dp)
                .align(CenterHorizontally),
            text = pokemonDetail.name,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
        )

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