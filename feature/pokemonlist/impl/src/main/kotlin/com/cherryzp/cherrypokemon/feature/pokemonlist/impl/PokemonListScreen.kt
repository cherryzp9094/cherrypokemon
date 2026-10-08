package com.cherryzp.cherrypokemon.feature.pokemonlist.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.cherryzp.cherrypokemon.core.model.Pokemon
import com.cherryzp.cherrypokemon.core.ui.PokemonCard

@Composable
internal fun PokemonListScreen(
    viewModel: PokemonListViewModel,
    onPokemonClick: (pokeId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    PokemonListScreen(
        pokemons = viewModel.pokemons.collectAsLazyPagingItems(),
        onPokemonClick = onPokemonClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PokemonListScreen(
    pokemons: LazyPagingItems<Pokemon>,
    onPokemonClick: (pokeId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.feature_pokemonlist_impl_title)) })
        }
    ) { padding ->
        // 로컬 캐시(source)와 네트워크(mediator)를 나눠 본다. 캐시가 있으면 목록을 보여준다.
        val localRefresh = pokemons.loadState.source.refresh
        val remoteRefresh = pokemons.loadState.mediator?.refresh

        when {
            localRefresh is LoadState.Loading -> FullScreenBox(padding) {
                CircularProgressIndicator()
            }

            pokemons.itemCount == 0 && remoteRefresh is LoadState.Error -> FullScreenBox(padding) {
                ErrorContent(onRetryClick = pokemons::retry)
            }

            pokemons.itemCount == 0 -> FullScreenBox(padding) {
                Text(stringResource(R.string.feature_pokemonlist_impl_empty))
            }

            else -> PokemonGrid(
                pokemons = pokemons,
                onPokemonClick = onPokemonClick,
                contentPadding = padding
            )
        }
    }
}

@Composable
private fun PokemonGrid(
    pokemons: LazyPagingItems<Pokemon>,
    onPokemonClick: (pokeId: Int) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        items(
            count = pokemons.itemCount,
            // 안정적인 key 가 없으면 앞에 아이템이 끼어들 때 다른 카드에 상태가 붙는다.
            key = pokemons.itemKey { it.id }
        ) { index ->
            val pokemon = pokemons[index]
            if (pokemon != null) {
                PokemonCard(
                    name = pokemon.name,
                    number = pokemon.id,
                    imageUrl = pokemon.imageUrl,
                    onClick = { onPokemonClick(pokemon.id) }
                )
            }
        }

        when (pokemons.loadState.append) {
            is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                ErrorContent(onRetryClick = pokemons::retry)
            }

            else -> Unit
        }
    }
}

@Composable
private fun ErrorContent(onRetryClick: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.feature_pokemonlist_impl_error_load),
            style = MaterialTheme.typography.bodyLarge
        )
        Button(onClick = onRetryClick) {
            Text(stringResource(R.string.feature_pokemonlist_impl_retry))
        }
    }
}

@Composable
private fun FullScreenBox(padding: PaddingValues, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
