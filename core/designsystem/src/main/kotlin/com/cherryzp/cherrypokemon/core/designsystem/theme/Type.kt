package com.cherryzp.cherrypokemon.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 타이포그래피. 화면에서 `fontSize` 나 `letterSpacing` 을 직접 쓰지 않는다.
 *
 * 기본 Material 3 척도에서 두 가지만 바꿨다.
 * - 제목 계열을 굵게 해 숫자·이름이 먼저 읽히게 한다
 * - `labelMedium` 에 자간을 넣어 구간 제목에 쓴다
 */
internal val CherryPokemonTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontWeight = FontWeight.Black),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        // 구간 제목. 작게 쓰고 자간으로 구분한다.
        labelMedium = TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
    )
}
