package com.cherryzp.cherrypokemon.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Fire,
    onPrimary = Color.White,
    primaryContainer = FireContainer,
    onPrimaryContainer = Fire,
    secondary = Water,
    onSecondary = Color.White,
    secondaryContainer = WaterContainer,
    onSecondaryContainer = Water,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = Fire,
    onPrimary = Color.Black,
    primaryContainer = FireDark,
    onPrimaryContainer = FireContainer,
    secondary = Water,
    onSecondary = Color.Black,
    secondaryContainer = WaterDark,
    onSecondaryContainer = WaterContainer,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface
)

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
        content = content
    )
}
