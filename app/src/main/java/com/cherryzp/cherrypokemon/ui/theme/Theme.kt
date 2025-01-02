package com.cherryzp.cherrypokemon.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LightColorScheme = lightColorScheme(
    primary = PrimaryFire,
    onPrimary = TextOnPrimary,
    primaryContainer = Color(0xFFFFDAB5),
    onPrimaryContainer = PrimaryFire,
    secondary = PrimaryWater,
    onSecondary = TextOnPrimary,
    secondaryContainer = Color(0xFFBFD9FF),
    onSecondaryContainer = PrimaryWater,
    background = NeutralBackground,
    onBackground = TextOnBackground,
    surface = NeutralSurface,
    onSurface = TextOnBackground
)

val DarkColorScheme = darkColorScheme(
    primary = PrimaryFire,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF8C3900),
    onPrimaryContainer = PrimaryFire,
    secondary = PrimaryWater,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF003C8A),
    onSecondaryContainer = PrimaryWater,
    background = NeutralBackgroundDark,
    onBackground = TextOnBackgroundDark,
    surface = NeutralSurfaceDark,
    onSurface = TextOnBackgroundDark
)

@Composable
fun CherryPokemonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            activity?.let {
                val window = it.window
                window.statusBarColor = PrimaryFire.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}