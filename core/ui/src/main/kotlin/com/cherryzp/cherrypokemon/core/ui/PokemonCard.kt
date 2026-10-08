package com.cherryzp.cherrypokemon.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.palette.PalettePlugin

/**
 * 목록에 쓰는 포켓몬 카드. 배경색은 이미지에서 뽑은 대표 색이다.
 *
 * 도메인 모델이 아니라 필요한 값만 받는다.
 */
@Composable
fun PokemonCard(
    name: String,
    number: Int,
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var backgroundColor by remember(imageUrl) { mutableStateOf<Color?>(null) }
    val cardColor = backgroundColor ?: MaterialTheme.colorScheme.surfaceVariant
    val contentColor = contentColorOn(cardColor)

    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = cardColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.core_ui_pokemon_number, number),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium
            )
            GlideImage(
                imageModel = { imageUrl },
                // 이미 받은 비트맵에서 대표 색을 뽑는다. 이미지를 다시 받지 않는다.
                component = rememberImageComponent {
                    +PalettePlugin(
                        imageModel = imageUrl,
                        useCache = true,
                        paletteLoadedListener = { palette ->
                            palette.dominantSwatch?.let { backgroundColor = Color(it.rgb) }
                        }
                    )
                },
                // 로딩 전후 크기가 같아야 목록이 튀지 않는다.
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }
    }
}
