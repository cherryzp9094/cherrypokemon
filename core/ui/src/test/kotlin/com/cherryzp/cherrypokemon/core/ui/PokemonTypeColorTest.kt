package com.cherryzp.cherrypokemon.core.ui

import androidx.compose.ui.graphics.Color
import com.cherryzp.cherrypokemon.core.model.PokemonType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 타입 칩과 목록 카드가 이 색을 쓴다. 대비가 깨지면 글자가 안 읽힌다. */
class PokemonTypeColorTest {

    @Test
    fun everyTypeColor_hasReadableContentColor() {
        val failures = PokemonType.entries.mapNotNull { type ->
            val ratio = contrastRatio(type.color, contentColorOn(type.color))
            // WCAG AA 의 작은 글자 기준.
            if (ratio < 4.5f) "${type.name} ${"%.2f".format(ratio)}:1" else null
        }

        assertEquals(emptyList<String>(), failures)
    }

    @Test
    fun contentColorOn_lightBackground_isBlack() {
        // 풀(#78C850)은 휘도 0.5 미만이지만 흰 글자로는 2.06:1 밖에 되지 않는다.
        assertEquals(Color.Black, contentColorOn(PokemonType.Grass.color))
        assertEquals(Color.Black, contentColorOn(Color.White))
    }

    @Test
    fun contentColorOn_darkBackground_isWhite() {
        assertEquals(Color.White, contentColorOn(PokemonType.Dragon.color))
        assertEquals(Color.White, contentColorOn(Color.Black))
    }

    @Test
    fun contrastRatio_knownPairs() {
        assertEquals(21f, contrastRatio(Color.Black, Color.White), 0.01f)
        assertEquals(1f, contrastRatio(Color.Red, Color.Red), 0.01f)
        // 순서를 바꿔도 같다.
        assertTrue(
            contrastRatio(Color.Black, Color.White) == contrastRatio(Color.White, Color.Black)
        )
    }
}
