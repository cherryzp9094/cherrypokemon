package com.cherryzp.cherrypokemon.ui.view.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.cherryzp.domain.model.Pokemon
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun MainScreen(
    pokemons: LazyPagingItems<Pokemon>?
) {
    LazyColumn {
        pokemons?.let {
            items(pokemons.itemCount) { index ->
                Column {
                    Text(
                        text = "No.${pokemons[index]?.id}. ${pokemons[index]?.name.orEmpty()}",
                        style = MaterialTheme.typography.h4
                    )
                    GlideImage(
                        modifier = Modifier.size(300.dp),
                        imageModel = { pokemons[index]?.imageUrl }
                    )
                }
            }
        }
    }
}