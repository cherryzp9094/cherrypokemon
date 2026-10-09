package com.cherryzp.cherrypokemon.feature.pokemondetail.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cherryzp.cherrypokemon.core.model.PokemonAbility
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.model.PokemonStats
import com.cherryzp.cherrypokemon.core.ui.DetailRow
import com.cherryzp.cherrypokemon.core.ui.DetailSectionLabel
import com.cherryzp.cherrypokemon.core.ui.StatBar
import com.cherryzp.cherrypokemon.core.ui.TypeChip
import com.cherryzp.cherrypokemon.core.ui.color
import com.skydoves.landscapist.ImageOptions
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

@Composable
internal fun PokemonDetailScreen(
    uiState: PokemonDetailUiState,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 머리말 띠가 상태 바까지 올라가야 해서 Scaffold 대신 직접 겹쳐 그린다.
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            uiState.showsLoading -> CircularProgressIndicator()
            uiState.showsError -> ErrorContent(onRetryClick = onRetryClick)
            uiState.pokemonDetail != null -> PokemonDetailContent(uiState.pokemonDetail)
        }

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(4.dp)
                .zIndex(2f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.feature_pokemondetail_impl_back)
            )
        }
    }
}

@Composable
private fun PokemonDetailContent(pokemonDetail: PokemonDetail) {
    // 첫 타입 색으로 머리말 띠를 칠한다. 포켓몬마다 인상이 달라진다.
    val accent = pokemonDetail.types.firstOrNull()?.color
        ?: MaterialTheme.colorScheme.primary

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // 내용이 짧아도 둥근 면이 화면 끝까지 닿아야 띠 색이 아래에 남지 않는다.
        val minContentHeight = maxHeight - BAND_HEIGHT

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(accent.copy(alpha = 0.12f))
                .verticalScroll(rememberScrollState())
        ) {
            HeaderBand(pokemonDetail = pokemonDetail, accent = accent)

            // 내용은 위쪽 모서리를 둥글린 면에 올려 이미지 영역과 나눈다.
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = minContentHeight),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 22.dp)
                ) {
                    NameBlock(pokemonDetail = pokemonDetail)

                    DetailSectionLabel(
                        stringResource(R.string.feature_pokemondetail_impl_section_stats)
                    )
                    StatsBlock(stats = pokemonDetail.stats, accent = accent)

                    DetailSectionLabel(
                        stringResource(R.string.feature_pokemondetail_impl_section_basics)
                    )
                    BasicsBlock(pokemonDetail = pokemonDetail)
                }
            }
        }
    }
}

/** 머리말 띠 높이. 상태 바와 뒤로가기 버튼까지 덮는다. */
private val BAND_HEIGHT = 300.dp

@Composable
private fun HeaderBand(pokemonDetail: PokemonDetail, accent: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BAND_HEIGHT)
            // 아래 둥근 면보다 나중에 그려야 걸친 이미지가 가려지지 않는다.
            .zIndex(1f)
    ) {
        Text(
            text = "%03d".format(pokemonDetail.id),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Black,
            color = accent.copy(alpha = 0.30f),
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp, top = 32.dp)
        )
        GlideImage(
            imageModel = { pokemonDetail.imageUrl },
            // 기본값인 Crop 은 이미지를 영역에 맞춰 잘라낸다. 전체가 보여야 한다.
            imageOptions = ImageOptions(contentScale = ContentScale.Fit),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp)
                .size(216.dp)
                // 둥근 면 위로 걸치게 해서 두 영역이 이어져 보이게 한다.
                .offset(y = 16.dp)
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
