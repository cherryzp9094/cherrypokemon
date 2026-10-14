package com.cherryzp.cherrypokemon.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.palette.PalettePlugin

/**
 * 목록에 쓰는 포켓몬 카드.
 *
 * 카드는 흰 면이고, 이미지에서 뽑은 대표 색은 **이미지 뒤 타원에만** 쓴다.
 * 색을 배경 전체에 깔면 포켓몬 그림과 색이 서로 싸우고, 글자 대비도 색마다 달라진다.
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
    var accent by remember(imageUrl) { mutableStateOf<Color?>(null) }
    val tint = accent ?: MaterialTheme.colorScheme.surfaceVariant

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .fillMaxSize(0.86f)
                        .clip(MaterialTheme.shapes.medium)
                        // 대표 색은 옅게만 쓴다.
                        .background(tint.copy(alpha = 0.18f))
                )
                GlideImage(
                    imageModel = { imageUrl },
                    // 이미 받은 비트맵에서 대표 색을 뽑는다. 이미지를 다시 받지 않는다.
                    component = rememberImageComponent {
                        +PalettePlugin(
                            imageModel = imageUrl,
                            useCache = true,
                            paletteLoadedListener = { palette ->
                                palette.dominantSwatch?.let { accent = Color(it.rgb) }
                            }
                        )
                    },
                    // 기본값인 Crop 은 이미지를 영역에 맞춰 잘라낸다.
                    imageOptions = ImageOptions(contentScale = ContentScale.Fit),
                    // 로딩 전후 크기가 같아야 목록이 튀지 않는다.
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(
                text = stringResource(R.string.core_ui_pokemon_number, number),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
