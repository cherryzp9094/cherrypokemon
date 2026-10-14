package com.cherryzp.cherrypokemon.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 배경·전경 쌍의 대비를 고정한다.
 *
 * 색을 바꿀 때 짝을 같이 고치지 않으면 여기서 걸린다.
 */
class ColorSchemeContrastTest {

    @Test
    fun lightScheme_everyPair_meetsAaForSmallText() = assertPairs(lightScheme())

    @Test
    fun darkScheme_everyPair_meetsAaForSmallText() = assertPairs(darkScheme())

    private fun assertPairs(scheme: ColorScheme) {
        val pairs = listOf(
            "primary" to (scheme.primary to scheme.onPrimary),
            "primaryContainer" to (scheme.primaryContainer to scheme.onPrimaryContainer),
            "secondary" to (scheme.secondary to scheme.onSecondary),
            "secondaryContainer" to (scheme.secondaryContainer to scheme.onSecondaryContainer),
            "surface" to (scheme.surface to scheme.onSurface),
            "surfaceVariant" to (scheme.surfaceVariant to scheme.onSurfaceVariant),
            "background" to (scheme.background to scheme.onBackground),
            // 보조 글자와 구분선에 쓴다.
            "surface/outline" to (scheme.surface to scheme.outline)
        )

        val failures = pairs.mapNotNull { (name, colors) ->
            val ratio = contrastRatio(colors.first, colors.second)
            val required = if (name == "surface/outline") 3f else 4.5f
            if (ratio < required) "$name ${"%.2f".format(ratio)}:1 (필요 $required)" else null
        }

        assertEquals(emptyList<String>(), failures)
    }
}
