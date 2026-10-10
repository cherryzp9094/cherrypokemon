package com.cherryzp.cherrypokemon.core.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import com.cherryzp.cherrypokemon.core.model.AppLanguage
import java.util.Locale

/**
 * [language] 로 문자열 리소스를 읽게 한다.
 *
 * 기기 언어를 바꾸는 것이 아니라 Compose 트리 안에서만 바꾼다. AppCompat 없이
 * minSdk 26 부터 같은 방식으로 동작하고, 설정을 바꾸면 화면이 바로 다시 그려진다.
 */
@Composable
fun ProvideAppLocale(language: AppLanguage, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val localized = remember(context, configuration, language) {
        val localizedConfiguration = Configuration(configuration).apply {
            setLocale(Locale.forLanguageTag(language.tag))
        }
        context.createConfigurationContext(localizedConfiguration)
    }

    CompositionLocalProvider(
        LocalContext provides localized,
        LocalConfiguration provides localized.resources.configuration,
        LocalResources provides localized.resources,
        content = content
    )
}
