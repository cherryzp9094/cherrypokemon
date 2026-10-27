package com.cherryzp.cherrypokemon.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cherryzp.cherrypokemon.core.designsystem.theme.contentColorOn
import com.cherryzp.cherrypokemon.core.designsystem.theme.contrastRatio

/**
 * 상세 화면의 구간 제목. 카드 대신 가는 선으로 구간을 끊는다.
 */
@Composable
fun DetailSectionLabel(text: String, modifier: Modifier = Modifier) {
    // 구간 사이는 넉넉하게, 제목과 내용 사이는 촘촘하게. 그래야 제목이 아래 내용에 붙어 보인다.
    Column(modifier.fillMaxWidth().padding(top = 28.dp, bottom = 12.dp)) {
        Text(
            text = text,
            // 자간은 labelMedium 토큰이 들고 있다.
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}

/**
 * 이름과 값을 양 끝에 두는 줄. 기본 정보 표에 쓴다.
 */
@Composable
fun DetailRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        // 줄 사이 간격은 묶는 쪽이 정한다. 여기서 아래 여백을 넣으면 마지막 줄 뒤에도 남아
        // 다음 구간과의 간격이 들쭉날쭉해진다.
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * 종족값 한 줄. 이름과 수치를 양 끝에 두고 아래에 가는 막대를 둔다.
 *
 * @param maxValue 막대 길이를 정하는 상한
 */
@Composable
fun StatBar(
    label: String,
    value: Int,
    maxValue: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            // 이름과 수치를 따로 읽지 않고 한 번에 읽어 준다.
            .semantics { contentDescription = "$label $value" }
    ) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.labelLarge
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.shapes.extraSmall
                )
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction = (value.toFloat() / maxValue).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(color, MaterialTheme.shapes.extraSmall)
            )
        }
    }
}

/** 타입과 배율을 보여주는 칩. */
@Composable
fun TypeChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColorOn(color),
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
        )
    }
}
