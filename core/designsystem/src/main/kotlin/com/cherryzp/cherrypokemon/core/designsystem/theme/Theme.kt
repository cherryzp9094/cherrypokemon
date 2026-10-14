package com.cherryzp.cherrypokemon.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DexRed,
    onPrimary = Color.White,
    primaryContainer = DexRedContainer,
    onPrimaryContainer = OnDexRedContainer,
    secondary = Slate,
    onSecondary = Color.White,
    secondaryContainer = SlateContainer,
    onSecondaryContainer = OnSlateContainer,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = DexRedLight,
    onPrimary = Color(0xFF690003),
    primaryContainer = DexRedContainerDark,
    onPrimaryContainer = DexRedContainer,
    secondary = SlateLight,
    onSecondary = Color(0xFF26303D),
    secondaryContainer = SlateContainerDark,
    onSecondaryContainer = SlateContainer,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

/** 테스트가 대비를 검사한다. */
internal fun lightScheme() = LightColorScheme

internal fun darkScheme() = DarkColorScheme

/**
 * 앱 테마. 시스템 바 색은 칠하지 않는다. 화면이 edge-to-edge 로 그린다.
 *
 * dynamic color 는 쓰지 않는다. 포켓몬 타입 색과 브랜드 색을 기기 배경색이 덮어쓰면
 * 화면마다 색 의미가 달라진다.
 */
@Composable
fun CherryPokemonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = CherryPokemonTypography,
        shapes = CherryPokemonShapes,
        content = content
    )
}
