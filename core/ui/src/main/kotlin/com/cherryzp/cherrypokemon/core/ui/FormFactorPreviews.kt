package com.cherryzp.cherrypokemon.core.ui

import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

/**
 * 화면 하나를 폰·폴드·태블릿 크기로 함께 그린다.
 *
 * 스크린샷 테스트에 붙이면 CI 가 세 크기를 다 비교한다. 폴드(673×841dp)와 태블릿(1280×800dp)은
 * 에뮬레이터 없이는 보기 어려워서, 여기서 안 잡으면 큰 화면 깨짐은 기기에서야 드러난다.
 */
@Preview(name = "Phone", device = Devices.PHONE, showBackground = true)
@Preview(name = "Foldable", device = Devices.FOLDABLE, showBackground = true)
@Preview(name = "Tablet", device = Devices.TABLET, showBackground = true)
annotation class FormFactorPreviews
