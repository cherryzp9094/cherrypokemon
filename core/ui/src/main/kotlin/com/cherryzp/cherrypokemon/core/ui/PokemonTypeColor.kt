package com.cherryzp.cherrypokemon.core.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.cherryzp.cherrypokemon.core.model.PokemonType

/** 타입을 화면에 표시할 색으로 바꾼다. 도메인 모델은 색을 모른다. */
val PokemonType.color: Color
    get() = when (this) {
        PokemonType.Normal -> Color(0xFFA8A878)
        PokemonType.Fighting -> Color(0xFFC03028)
        PokemonType.Flying -> Color(0xFFA890F0)
        PokemonType.Poison -> Color(0xFFA040A0)
        PokemonType.Ground -> Color(0xFFE0C068)
        PokemonType.Rock -> Color(0xFFB8A038)
        PokemonType.Bug -> Color(0xFFA8B820)
        PokemonType.Ghost -> Color(0xFF705898)
        PokemonType.Steel -> Color(0xFFB8B8D0)
        PokemonType.Fire -> Color(0xFFF08030)
        PokemonType.Water -> Color(0xFF6890F0)
        PokemonType.Grass -> Color(0xFF78C850)
        PokemonType.Electric -> Color(0xFFF8D030)
        PokemonType.Psychic -> Color(0xFFF85888)
        PokemonType.Ice -> Color(0xFF98D8D8)
        PokemonType.Dragon -> Color(0xFF7038F8)
        PokemonType.Dark -> Color(0xFF705848)
        PokemonType.Fairy -> Color(0xFFEE99AC)
        PokemonType.Stellar -> Color(0xFFFFD700)
        PokemonType.Shadow -> Color(0xFF606060)
        PokemonType.Unknown -> Color(0xFF68A090)
    }

/**
 * 배경색 위에 올릴 글자색.
 *
 * 검정과 흰색 각각의 대비를 재서 높은 쪽을 고른다. 휘도 0.5 를 기준으로 나누면
 * 밝은 배경(풀 `#78C850` 등)에 흰 글자가 얹혀 대비가 2:1 까지 떨어진다.
 * 두 색의 대비가 같아지는 휘도는 약 0.179 다.
 */
fun contentColorOn(background: Color): Color =
    if (contrastRatio(background, Color.White) >= contrastRatio(background, Color.Black)) {
        Color.White
    } else {
        Color.Black
    }

/**
 * 두 색의 [WCAG 명도 대비](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html).
 *
 * 1(같은 색)부터 21(검정과 흰색)까지다. 작은 글자는 4.5 이상이어야 한다.
 * 반투명 색은 뒤에 깔린 색을 알 수 없어 그대로 계산한다.
 */
fun contrastRatio(a: Color, b: Color): Float {
    val lighter = maxOf(a.luminance(), b.luminance())
    val darker = minOf(a.luminance(), b.luminance())
    return (lighter + 0.05f) / (darker + 0.05f)
}
