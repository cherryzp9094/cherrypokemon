package com.cherryzp.cherrypokemon.ui.component

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PokemonCard(
    id: Int,
    name: String,
    image: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(
                Shapes().medium
            ).background(
                Color.White
            ).border(
                width = 2.dp,
                color = Color.LightGray,
                shape = Shapes().medium
            ).background(extractDominantColor(image.orEmpty()))
            .padding(8.dp)
    ) {
        Text(
            text = "No.$id",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
        )

        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
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

@Composable
fun extractDominantColor(url: String): Color {
    var dominantColor by remember { mutableStateOf(Color.White) }
    val context = LocalContext.current
    LaunchedEffect(url) {
        withContext(Dispatchers.IO) {
            try {
                val bitmap = Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .submit()
                    .get()

                val palette = Palette.from(bitmap).generate()
                val color = palette.getDominantColor(0xFFFFFF)

                dominantColor = Color(color)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    return dominantColor
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