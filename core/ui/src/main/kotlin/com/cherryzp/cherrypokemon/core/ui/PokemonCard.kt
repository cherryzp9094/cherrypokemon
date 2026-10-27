package com.cherryzp.cherrypokemon.core.ui

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cherryzp.cherrypokemon.core.designsystem.theme.contentColorOn
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.palette.PalettePlugin

/**
 * 목록에 쓰는 포켓몬 카드.
 *
 * 카드 전체를 이미지에서 뽑은 대표 색으로 칠한다. 도감 번호는 상세 화면 머리말처럼
 * 큰 워터마크로 깔아 두 화면이 한 가족으로 보이게 하고, 이름은 아래에 둬 그림이 위를 차지하게 한다.
 *
 * 글자색은 배경에서 계산해 대비를 지킨다. 보조 글자는 회색이 아니라 같은 전경색을 옅게 쓴다.
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
    val shape = MaterialTheme.shapes.large

    // 어두운 배경에서는 색 그림자가 카드 둘레에 빛나는 테두리처럼 번진다. 밝은 테마에서만 쓴다.
    val isLightTheme = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    Card(
        onClick = onClick,
        modifier = modifier.then(
            if (isLightTheme) {
                // 그림자도 카드 색을 따라가야 색 면이 떠 보인다. 회색 그림자는 색을 탁하게 만든다.
                Modifier.shadow(6.dp, shape, ambientColor = cardColor, spotColor = cardColor)
            } else {
                Modifier
            }
        ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = cardColor,
            contentColor = contentColor
        )
    ) {
        // 카드 높이를 고정해야 이름 길이가 달라도 줄이 가지런하다.
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(0.8f)) {
            Text(
                text = "%03d".format(number),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = contentColor.copy(alpha = 0.16f),
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 10.dp, top = 2.dp)
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
                // 기본값인 Crop 은 이미지를 영역에 맞춰 잘라낸다.
                imageOptions = ImageOptions(contentScale = ContentScale.Fit),
                // 그림은 위 3/4 에 둔다. 이름 자리를 침범하면 글자가 그림에 묻힌다.
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 22.dp)
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f)
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.core_ui_pokemon_number, number),
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.72f)
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
