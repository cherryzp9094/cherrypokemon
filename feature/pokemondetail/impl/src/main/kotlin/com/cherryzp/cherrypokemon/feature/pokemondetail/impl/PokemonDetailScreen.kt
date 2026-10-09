package com.cherryzp.cherrypokemon.feature.pokemondetail.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cherryzp.cherrypokemon.core.model.PokemonAbility
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.model.PokemonStats
import com.cherryzp.cherrypokemon.core.ui.DetailRow
import com.cherryzp.cherrypokemon.core.ui.DetailSectionLabel
import com.cherryzp.cherrypokemon.core.ui.StatBar
import com.cherryzp.cherrypokemon.core.ui.TypeChip
import com.cherryzp.cherrypokemon.core.ui.color
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
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(
                                R.string.feature_pokemondetail_impl_back
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
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
    // 첫 타입 색으로 머리말 띠를 칠한다. 포켓몬마다 인상이 달라진다.
    val accent = pokemonDetail.types.firstOrNull()?.color
        ?: MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        HeaderBand(pokemonDetail = pokemonDetail, accent = accent)

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp)) {
            NameBlock(pokemonDetail = pokemonDetail)

            DetailSectionLabel(stringResource(R.string.feature_pokemondetail_impl_section_stats))
            StatsBlock(stats = pokemonDetail.stats, accent = accent)

            DetailSectionLabel(stringResource(R.string.feature_pokemondetail_impl_section_basics))
            BasicsBlock(pokemonDetail = pokemonDetail)
        }
    }
}

@Composable
private fun HeaderBand(pokemonDetail: PokemonDetail, accent: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(accent.copy(alpha = 0.12f))
    ) {
        Text(
            text = "%03d".format(pokemonDetail.id),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Black,
            color = accent.copy(alpha = 0.25f),
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp)
        )
        GlideImage(
            imageModel = { pokemonDetail.imageUrl },
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(0.62f)
        )
    }
}

@Composable
private fun NameBlock(pokemonDetail: PokemonDetail) {
    Text(
        text = pokemonDetail.name,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.feature_pokemondetail_impl_number, pokemonDetail.id),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            pokemonDetail.types.forEach { type ->
                TypeChip(text = type.name, color = type.color)
            }
        }
    }
}

@Composable
private fun StatsBlock(stats: PokemonStats, accent: Color) {
    val rows = listOf(
        stringResource(R.string.feature_pokemondetail_impl_stat_hp) to stats.hp,
        stringResource(R.string.feature_pokemondetail_impl_stat_attack) to stats.attack,
        stringResource(R.string.feature_pokemondetail_impl_stat_defense) to stats.defense,
        stringResource(R.string.feature_pokemondetail_impl_stat_special_attack) to
            stats.specialAttack,
        stringResource(R.string.feature_pokemondetail_impl_stat_special_defense) to
            stats.specialDefense,
        stringResource(R.string.feature_pokemondetail_impl_stat_speed) to stats.speed
    )

    // 좁은 화면에서도 여섯 줄이 길지 않도록 두 칸으로 나눈다.
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        listOf(rows.take(3), rows.drop(3)).forEach { column ->
            Column(modifier = Modifier.weight(1f)) {
                column.forEach { (label, value) ->
                    StatBar(
                        label = label,
                        value = value,
                        maxValue = PokemonStats.MAX_VALUE,
                        color = accent
                    )
                }
            }
        }
    }
    DetailRow(
        label = stringResource(R.string.feature_pokemondetail_impl_stat_total),
        value = stats.total.toString()
    )
}

@Composable
private fun BasicsBlock(pokemonDetail: PokemonDetail) {
    DetailRow(
        label = stringResource(R.string.feature_pokemondetail_impl_height),
        value = stringResource(
            R.string.feature_pokemondetail_impl_height_value,
            pokemonDetail.heightMeters
        )
    )
    DetailRow(
        label = stringResource(R.string.feature_pokemondetail_impl_weight),
        value = stringResource(
            R.string.feature_pokemondetail_impl_weight_value,
            pokemonDetail.weightKilograms
        )
    )
    DetailRow(
        label = stringResource(R.string.feature_pokemondetail_impl_base_experience),
        value = pokemonDetail.baseExperience.toString()
    )
    pokemonDetail.abilities.forEach { ability ->
        DetailRow(label = ability.label(), value = ability.name)
    }
}

@Composable
private fun PokemonAbility.label(): String = stringResource(
    if (isHidden) {
        R.string.feature_pokemondetail_impl_hidden_ability
    } else {
        R.string.feature_pokemondetail_impl_ability
    }
)

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
