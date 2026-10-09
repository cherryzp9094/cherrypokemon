package com.cherryzp.cherrypokemon.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.cherryzp.cherrypokemon.core.designsystem.theme.CherryPokemonTheme
import com.cherryzp.cherrypokemon.core.model.PokemonStats
import com.cherryzp.cherrypokemon.core.model.PokemonType

/** 상세 화면 구성 요소의 모양을 고정한다. */
class DetailSectionScreenshotTest {

    @PreviewTest
    @PreviewLightDark
    @Composable
    fun detailSection() {
        CherryPokemonTheme {
            Surface {
                Column(Modifier.width(320.dp).padding(16.dp)) {
                    DetailSectionLabel("종족값")
                    StatBar("HP", 45, PokemonStats.MAX_VALUE, PokemonType.Grass.color)
                    StatBar("공격", 49, PokemonStats.MAX_VALUE, PokemonType.Grass.color)
                    StatBar("특공", 65, PokemonStats.MAX_VALUE, PokemonType.Grass.color)
                    DetailRow("합계", "318")
                    DetailSectionLabel("기본 정보")
                    DetailRow("키", "0.7 m")
                    DetailRow("특성", "심록")
                    DetailRow("숨겨진 특성", "엽록소")
                    Row(
                        Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TypeChip("풀", PokemonType.Grass.color)
                        TypeChip("독", PokemonType.Poison.color)
                    }
                }
            }
        }
    }
}
