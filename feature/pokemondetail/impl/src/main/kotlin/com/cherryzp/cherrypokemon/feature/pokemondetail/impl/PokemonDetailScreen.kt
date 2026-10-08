package com.cherryzp.cherrypokemon.feature.pokemondetail.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.ui.color
import com.cherryzp.cherrypokemon.core.ui.contentColorOn
import com.skydoves.landscapist.glide.GlideImage
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
internal fun PokemonDetailScreen(
    viewModel: PokemonDetailViewModel,
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.collectAsState()
    // 설정이 바뀌면 문자열도 다시 읽어야 한다.
    val resources = LocalResources.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PokemonDetailSideEffect.ShowMessage ->
                snackbarHostState.showSnackbar(resources.getString(sideEffect.messageId))
        }
    }

    PokemonDetailScreen(
        uiState = uiState,
        onRetryClick = viewModel::retry,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PokemonDetailScreen(
    uiState: PokemonDetailUiState,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.pokemonDetail?.name.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(
                                R.string.feature_pokemondetail_impl_back
                            )
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.showsLoading -> CircularProgressIndicator()
                uiState.showsError -> ErrorContent(onRetryClick = onRetryClick)
                uiState.pokemonDetail != null -> PokemonDetailContent(uiState.pokemonDetail)
            }
        }
    }
}

@Composable
private fun PokemonDetailContent(pokemonDetail: PokemonDetail) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlideImage(
            imageModel = { pokemonDetail.imageUrl },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            pokemonDetail.types.forEach { type ->
                Surface(
                    color = type.color,
                    contentColor = contentColorOn(type.color),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = type.name,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LabeledValue(
                label = stringResource(R.string.feature_pokemondetail_impl_height),
                value = stringResource(
                    R.string.feature_pokemondetail_impl_height_value,
                    pokemonDetail.heightMeters
                )
            )
            LabeledValue(
                label = stringResource(R.string.feature_pokemondetail_impl_weight),
                value = stringResource(
                    R.string.feature_pokemondetail_impl_weight_value,
                    pokemonDetail.weightKilograms
                )
            )
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ErrorContent(onRetryClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.feature_pokemondetail_impl_error_load),
            style = MaterialTheme.typography.bodyLarge
        )
        Button(onClick = onRetryClick) {
            Text(stringResource(R.string.feature_pokemondetail_impl_retry))
        }
    }
}
