package com.cherryzp.cherrypokemon.ui.view.pokemonDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cherryzp.domain.model.PokemonDetail

@Composable
fun PokemonDetailScreen(
    pokemonDetail: PokemonDetail
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = pokemonDetail.name)
        Text(text = pokemonDetail.height.toString())
        Text(text = pokemonDetail.weight.toString())

        Text(text = pokemonDetail.toString())
    }

}