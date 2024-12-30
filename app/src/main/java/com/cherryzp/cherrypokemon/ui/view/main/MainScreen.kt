package com.cherryzp.cherrypokemon.ui.view.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.cherryzp.cherrypokemon.extend.fetchDominantColor
import com.cherryzp.cherrypokemon.ui.component.PokemonCard
import com.cherryzp.domain.model.Pokemon
import kotlinx.collections.immutable.ImmutableMap

@Composable
fun MainScreen(
    pokemons: LazyPagingItems<Pokemon>?,
    pokemonBackgroundColor: ImmutableMap<Int, Color>,
    updateBackgroundColor: (Int, Color) -> Unit,
    goPokemonDetail: (Int) -> Unit
) {
    val context = LocalContext.current

    pokemons?.let {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pokemons.itemCount) { index ->
                pokemons[index]?.let { pokemon ->
                    pokemonBackgroundColor[pokemon.id]?.let {
                        LaunchedEffect(pokemon.id) {
                            val color = fetchDominantColor(context, pokemon.imageUrl)
                            updateBackgroundColor(pokemon.id, color)
                        }
                    }

                    PokemonCard(
                        id = pokemon.id,
                        name = pokemon.name,
                        image = pokemon.imageUrl,
                        pokemonBackground = pokemonBackgroundColor[pokemon.id] ?: Color.White,
                        modifier = Modifier
                            .clickable { goPokemonDetail(pokemon.id) }
                    )
                }
            }
        }
    }
}