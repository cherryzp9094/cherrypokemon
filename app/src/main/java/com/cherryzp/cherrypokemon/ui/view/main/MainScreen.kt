package com.cherryzp.cherrypokemon.ui.view.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.cherryzp.cherrypokemon.ui.component.PokemonCard
import com.cherryzp.data.extend.default
import com.cherryzp.domain.model.Pokemon
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun MainScreen(
    pokemons: LazyPagingItems<Pokemon>?,
    goPokemonDetail: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        pokemons?.let {
            items(pokemons.itemCount) { index ->
                PokemonCard(
                    id = pokemons[index]?.id.default(),
                    name = pokemons[index]?.name.orEmpty(),
                    image = pokemons[index]?.imageUrl,
                    modifier = Modifier
                        .clickable { goPokemonDetail(pokemons[index]?.id.default()) }
                )
            }
        }
    }
}