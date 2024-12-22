package com.cherryzp.cherrypokemon.ui.view.pokemonDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cherryzp.domain.model.PokemonDetail
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun PokemonDetailScreen(
    pokemonDetail: PokemonDetail
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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