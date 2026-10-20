package com.cherryzp.cherrypokemon.feature.pokemondetail.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.cherryzp.cherrypokemon.core.model.AppLanguage
import com.cherryzp.cherrypokemon.core.model.EvolutionCondition
import com.cherryzp.cherrypokemon.core.model.EvolutionStage
import com.cherryzp.cherrypokemon.core.model.PokemonAbility
import com.cherryzp.cherrypokemon.core.model.PokemonDetail
import com.cherryzp.cherrypokemon.core.model.PokemonStats
import com.cherryzp.cherrypokemon.core.model.TypeMatchup
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
    onPokemonClick: (pokeId: Int) -> Unit,
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
        onPokemonClick = onPokemonClick,
        modifier = modifier
    )
}

@Composable
internal fun PokemonDetailScreen(
    uiState: PokemonDetailUiState,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
    onPokemonClick: (pokeId: Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // 머리말 띠가 상태 바까지 올라가야 해서 Scaffold 대신 직접 겹쳐 그린다.
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            uiState.showsLoading -> CircularProgressIndicator()

            uiState.showsError -> ErrorContent(onRetryClick = onRetryClick)

            uiState.pokemonDetail != null -> PokemonDetailContent(
                pokemonDetail = uiState.pokemonDetail,
                onPokemonClick = onPokemonClick,
                onBackClick = onBackClick
            )
        }

        // 로딩·에러일 때는 머리말이 없어서 뒤로가기를 따로 그린다.
        if (uiState.pokemonDetail == null) {
            BackButton(onBackClick = onBackClick, modifier = Modifier.align(Alignment.TopStart))
        }
    }
}

@Composable
private fun BackButton(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onBackClick,
        modifier = modifier
            .statusBarsPadding()
            .padding(4.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.feature_pokemondetail_impl_back)
        )
    }
}

@Composable
private fun PokemonDetailContent(
    pokemonDetail: PokemonDetail,
    onPokemonClick: (pokeId: Int) -> Unit,
    onBackClick: () -> Unit,
) {
    // 위에서 이미 고른 언어로 리소스를 읽고 있다.
    val language = AppLanguage.fromTag(LocalConfiguration.current.locales[0]?.toLanguageTag())
    // 첫 타입 색으로 머리말 띠를 칠한다. 포켓몬마다 인상이 달라진다.
    val accent = pokemonDetail.types.firstOrNull()?.type?.color
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
            HeaderBand(
                pokemonDetail = pokemonDetail,
                accent = accent,
                onBackClick = onBackClick
            )

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
                    NameBlock(pokemonDetail = pokemonDetail, language = language)

                    if (!pokemonDetail.flavorText.isEmpty) {
                        Text(
                            text = pokemonDetail.flavorText.of(language),
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(top = 14.dp)
                        )
                    }

                    DetailSectionLabel(
                        stringResource(R.string.feature_pokemondetail_impl_section_stats)
                    )
                    StatsBlock(stats = pokemonDetail.stats, accent = accent)

                    if (pokemonDetail.evolutionStages.size > 1) {
                        DetailSectionLabel(
                            stringResource(R.string.feature_pokemondetail_impl_section_evolution)
                        )
                        EvolutionBlock(
                            stages = pokemonDetail.evolutionStages,
                            currentId = pokemonDetail.id,
                            language = language,
                            onPokemonClick = onPokemonClick
                        )
                    }

                    if (pokemonDetail.typeMatchups.isNotEmpty()) {
                        DetailSectionLabel(
                            stringResource(R.string.feature_pokemondetail_impl_section_matchups)
                        )
                        MatchupBlock(
                            matchups = pokemonDetail.typeMatchups,
                            language = language
                        )
                    }

                    DetailSectionLabel(
                        stringResource(R.string.feature_pokemondetail_impl_section_basics)
                    )
                    BasicsBlock(pokemonDetail = pokemonDetail, language = language)
                }
            }
        }
    }
}

/** 한 구간 안에서 줄과 줄 사이. 구간 사이(28dp)보다 확실히 좁아야 묶여 보인다. */
private val DETAIL_ROW_GAP = 8.dp

/** 이 수까지는 진화 단계를 폭에 고르게 펼친다. 넘으면 가로로 스크롤한다. */
private const val MAX_EVOLUTION_STAGES_WITHOUT_SCROLL = 4

/** 머리말 띠 높이. 상태 바와 뒤로가기 버튼까지 덮는다. */
private val BAND_HEIGHT = 300.dp

@Composable
private fun HeaderBand(pokemonDetail: PokemonDetail, accent: Color, onBackClick: () -> Unit) {
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
                .padding(start = 24.dp, top = 32.dp)
        )
        // 머리말과 함께 올라간다. 고정하면 스크롤할 때 내용과 겹친다.
        BackButton(onBackClick = onBackClick, modifier = Modifier.align(Alignment.TopStart))
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
private fun NameBlock(pokemonDetail: PokemonDetail, language: AppLanguage) {
    Text(
        text = pokemonDetail.localizedName.of(language).ifEmpty { pokemonDetail.name },
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    if (!pokemonDetail.genus.isEmpty) {
        Text(
            text = pokemonDetail.genus.of(language),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.feature_pokemondetail_impl_number, pokemonDetail.id),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            pokemonDetail.types.forEach { type ->
                TypeChip(
                    text = type.name.of(language).ifEmpty { type.type.name },
                    color = type.type.color
                )
            }
        }
    }
}

/**
 * 진화 단계를 가로로 늘어놓는다. 분기(이브이)는 같은 단계에 여럿이라 가로로 스크롤된다.
 *
 * 아직 열어보지 않은 포켓몬은 한국어 이름이 없어 식별자로 떨어뜨린다.
 */
@Composable
private fun EvolutionBlock(
    stages: List<EvolutionStage>,
    currentId: Int,
    language: AppLanguage,
    onPokemonClick: (pokeId: Int) -> Unit,
) {
    // 서너 단계면 폭에 고르게 펼친다. 왼쪽에 몰리면 오른쪽이 비어 보인다.
    // 이브이처럼 많으면 폭에 다 들어가지 않으므로 가로로 스크롤한다.
    val fits = stages.size <= MAX_EVOLUTION_STAGES_WITHOUT_SCROLL
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (fits) Modifier else Modifier.horizontalScroll(rememberScrollState()))
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (fits) Arrangement.SpaceBetween else Arrangement.spacedBy(4.dp)
    ) {
        stages.forEachIndexed { index, stage ->
            if (index > 0 && stage.stage != stages[index - 1].stage) {
                EvolutionArrow(condition = stage.condition)
            }
            EvolutionStageItem(
                stage = stage,
                isCurrent = stage.pokemonId == currentId,
                language = language,
                onClick = { onPokemonClick(stage.pokemonId) }
            )
        }
    }
}

@Composable
private fun EvolutionStageItem(
    stage: EvolutionStage,
    isCurrent: Boolean,
    language: AppLanguage,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(84.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(enabled = !isCurrent, onClick = onClick)
            .padding(top = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlideImage(
            imageModel = { stage.imageUrl },
            imageOptions = ImageOptions(contentScale = ContentScale.Fit),
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = stage.localizedName.of(language).ifEmpty { stage.name },
            style = MaterialTheme.typography.labelLarge,
            // 보고 있는 포켓몬만 진하게.
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrent) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** 단계 사이의 화살표와 조건. */
@Composable
private fun EvolutionArrow(condition: EvolutionCondition?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            // 옆의 조건 글자가 뜻을 전한다.
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        condition?.label()?.let { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

/** 조건 글자. 도구 이름은 아직 영어 식별자다. */
@Composable
private fun EvolutionCondition.label(): String? {
    val level = minLevel
    val item = item
    return when {
        level != null -> stringResource(
            R.string.feature_pokemondetail_impl_evolution_level,
            level
        )

        item != null -> item.replace('-', ' ')

        else -> null
    }
}

/** 약점·저항·무효를 한 줄씩. 1 배인 타입은 보여줄 게 없어 빠져 있다. */
@Composable
private fun MatchupBlock(matchups: List<TypeMatchup>, language: AppLanguage) {
    val groups = listOf(
        R.string.feature_pokemondetail_impl_weakness to matchups.filter { it.isWeakness },
        R.string.feature_pokemondetail_impl_resistance to matchups.filter { it.isResistance },
        R.string.feature_pokemondetail_impl_immunity to matchups.filter { it.isImmunity }
    )

    Column(verticalArrangement = Arrangement.spacedBy(DETAIL_ROW_GAP)) {
        groups.forEach { (labelId, group) ->
            if (group.isEmpty()) return@forEach
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = stringResource(labelId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(56.dp).padding(top = 4.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    group.forEach { matchup ->
                        TypeChip(
                            text = matchup.label(language),
                            color = matchup.type.type.color
                        )
                    }
                }
            }
        }
    }
}

/** 칩에 적을 글자. 무효는 배율을 적지 않는다. */
@Composable
private fun TypeMatchup.label(language: AppLanguage): String {
    val name = type.name.of(language).ifEmpty { type.type.name }
    if (isImmunity) return name
    val multiplier = stringResource(
        R.string.feature_pokemondetail_impl_multiplier,
        if (multiplier % 1f == 0f) multiplier.toInt().toString() else multiplier.toString()
    )
    return "$name $multiplier"
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
private fun BasicsBlock(pokemonDetail: PokemonDetail, language: AppLanguage) {
    Column(verticalArrangement = Arrangement.spacedBy(DETAIL_ROW_GAP)) {
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
            DetailRow(
                label = ability.label(),
                // 이름을 아직 받지 못했으면 식별자를 그대로 보여준다.
                value = ability.localizedName.of(language).ifEmpty { ability.name }
            )
        }
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
