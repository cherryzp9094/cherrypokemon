package com.cherryzp.cherrypokemon.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 모서리 반경. 화면에서 `RoundedCornerShape` 를 직접 쓰지 않고 이 값을 쓴다.
 *
 * - `extraSmall` 타입 칩처럼 작은 조각
 * - `small` 토글, 진화 항목
 * - `medium` 목록 카드
 * - `large` 상세 본문 면
 */
internal val CherryPokemonShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
