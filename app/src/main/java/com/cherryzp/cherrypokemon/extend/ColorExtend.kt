package com.cherryzp.cherrypokemon.extend

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun fetchDominantColor(context: Context, imageUrl: String): Color {
    return withContext(Dispatchers.IO) {
        try {
            val bitmap = Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .submit()
                .get()

            val palette = Palette.from(bitmap).generate()
            val color = palette.getDominantColor(0xFFFFFF)
            Color(color)
        } catch (e: Exception) {
            e.printStackTrace()
            Color.White
        }
    }
}