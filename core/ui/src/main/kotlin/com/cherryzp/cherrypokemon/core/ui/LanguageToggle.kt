package com.cherryzp.cherrypokemon.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cherryzp.cherrypokemon.core.model.AppLanguage

/** 국기를 눌러 언어를 고른다. 고른 쪽에만 배경이 들어간다. */
@Composable
fun LanguageToggle(
    language: AppLanguage,
    onLanguageClick: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        AppLanguage.entries.forEach { entry ->
            val selected = entry == language
            val label = stringResource(entry.labelId)
            Surface(
                modifier = Modifier
                    .size(width = 44.dp, height = 32.dp)
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onLanguageClick(entry) }
                    )
                    // 국기 이모지만으로는 읽어 줄 수 없다.
                    .semantics { contentDescription = label },
                shape = RoundedCornerShape(8.dp),
                color = if (selected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    Color.Transparent
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = entry.flag, fontSize = 17.sp)
                }
            }
        }
    }
}

/** 국기 이모지. 리소스가 아니라 언어에 딸린 값이라 여기 둔다. */
private val AppLanguage.flag: String
    get() = when (this) {
        AppLanguage.KOREAN -> "🇰🇷"
        AppLanguage.ENGLISH -> "🇺🇸"
    }

private val AppLanguage.labelId: Int
    get() = when (this) {
        AppLanguage.KOREAN -> R.string.core_ui_language_korean
        AppLanguage.ENGLISH -> R.string.core_ui_language_english
    }
