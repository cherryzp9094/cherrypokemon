package com.cherryzp.cherrypokemon.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/*
 * 색은 배경·전경을 쌍으로 정의한다. 짝이 맞지 않으면 ColorSchemeContrastTest 가 잡는다.
 *
 * 포켓몬 타입 색은 여기 없다. 타입 색은 데이터에 딸린 값이라 :core:ui 가 들고 있고,
 * 이 팔레트는 타입 색과 싸우지 않도록 채도를 낮춘 중립색으로 짰다.
 */

/** 도감의 붉은색. 강조에만 쓰고 넓은 면적에는 쓰지 않는다. */
internal val DexRed = Color(0xFFB3332B)
internal val DexRedLight = Color(0xFFFFB4AB)
internal val DexRedContainer = Color(0xFFFFDAD5)
internal val OnDexRedContainer = Color(0xFF410002)
internal val DexRedContainerDark = Color(0xFF7A1D18)

internal val Slate = Color(0xFF3F4A5A)
internal val SlateLight = Color(0xFFB9C6DC)
internal val SlateContainer = Color(0xFFDCE3F0)
internal val OnSlateContainer = Color(0xFF121A24)
internal val SlateContainerDark = Color(0xFF2A3442)

internal val LightBackground = Color(0xFFFAFAFC)
internal val LightSurface = Color(0xFFFFFFFF)

/** 카드 뒤 바탕. 흰 카드를 띄우는 용도다. */
internal val LightSurfaceVariant = Color(0xFFEEEFF4)
internal val LightOnSurface = Color(0xFF17171A)

/** 보조 글자. 흰 배경에서 5.3:1 이다. */
internal val LightOnSurfaceVariant = Color(0xFF63636B)
internal val LightOutline = Color(0xFF8A8A92)

internal val DarkBackground = Color(0xFF111215)
internal val DarkSurface = Color(0xFF191A1E)
internal val DarkSurfaceVariant = Color(0xFF24262B)
internal val DarkOnSurface = Color(0xFFE6E6EA)
internal val DarkOnSurfaceVariant = Color(0xFFB4B4BC)
internal val DarkOutline = Color(0xFF8E8E96)
