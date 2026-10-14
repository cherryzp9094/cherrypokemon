package com.cherryzp.cherrypokemon.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * 두 색의 [WCAG 명도 대비](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html).
 *
 * 1(같은 색)부터 21(검정과 흰색)까지다. 작은 글자는 4.5 이상, 큰 글자는 3 이상이어야 한다.
 * 반투명 색은 뒤에 깔린 색을 알 수 없어 그대로 계산한다.
 */
fun contrastRatio(a: Color, b: Color): Float {
    val lighter = maxOf(a.luminance(), b.luminance())
    val darker = minOf(a.luminance(), b.luminance())
    return (lighter + 0.05f) / (darker + 0.05f)
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
