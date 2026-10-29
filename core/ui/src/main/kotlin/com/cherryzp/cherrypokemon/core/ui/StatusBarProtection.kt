package com.cherryzp.cherrypokemon.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity

/**
 * 상태 바 뒤에 까는 옅은 그러데이션.
 *
 * 화면이 상태 바까지 그리므로, 스크롤하면 내용이 시계·배터리 아이콘과 겹친다.
 * 내용 위에 배경색을 조금 깔아 아이콘이 읽히게 한다. 내용 맨 위에 두고 다른 것보다 나중에 그린다.
 *
 * @param visible false 면 사라진다. 상태 바 뒤가 다른 색(상세 머리말)일 때 끈다.
 */
@Composable
fun StatusBarProtection(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    visible: Boolean = true,
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        label = "statusBarProtection"
    )
    // 상태 바보다 조금 더 내려와야 경계가 보이지 않는다.
    val height = with(LocalDensity.current) {
        (WindowInsets.statusBars.getTop(this) * PROTECTION_HEIGHT_RATIO).toDp()
    }
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer { this.alpha = alpha }
            .background(
                Brush.verticalGradient(
                    listOf(color, color.copy(alpha = 0.8f), Color.Transparent)
                )
            )
    )
}

private const val PROTECTION_HEIGHT_RATIO = 1.2f
