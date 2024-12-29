package com.cherryzp.cherrypokemon.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun PokemonCard(
    id: Int,
    name: String,
    image: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(
                Color.White,
                Shapes().medium
            ).border(
                width = 1.dp,
                color = Color.LightGray,
                shape = Shapes().medium
            ).padding(8.dp)
    ) {
        Text(
            text = "No.$id",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )

        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            maxLines = 1
        )

        GlideImage(
            modifier = Modifier
                .clip(Shapes().medium)
                .fillMaxWidth()
                .aspectRatio(1f),
            imageModel = { image }
        )
    }
}

@Preview
@Composable
private fun PokemonCardPreview() {
    PokemonCard(
        id = 1,
        name = "Pikachu",
        image = null
    )
}