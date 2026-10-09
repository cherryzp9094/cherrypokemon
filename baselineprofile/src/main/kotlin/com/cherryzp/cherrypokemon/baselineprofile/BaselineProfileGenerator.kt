package com.cherryzp.cherrypokemon.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 앱을 켜고 목록에서 상세로 들어가는 경로의 Baseline Profile 을 만든다.
 *
 * `./gradlew :app:generateBaselineProfile` 로 실행한다.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(packageName = PACKAGE_NAME) {
        pressHome()
        startActivityAndWait()

        // 목록은 네트워크를 받아 Room 에 저장한 뒤 그려진다. 카드가 보일 때까지 기다린다.
        val card = By.textStartsWith("No.")
        device.wait(Until.hasObject(card), LOAD_TIMEOUT_MS)
        device.waitForIdle()

        // 다시 그려지는 사이에 요소가 사라질 수 있어 매번 새로 찾는다.
        device.findObject(card)?.click()
        device.wait(Until.gone(card), NAVIGATION_TIMEOUT_MS)
        device.waitForIdle()

        device.pressBack()
        device.wait(Until.hasObject(card), NAVIGATION_TIMEOUT_MS)
    }

    private companion object {
        const val PACKAGE_NAME = "com.cherryzp.cherrypokemon"
        const val LOAD_TIMEOUT_MS = 15_000L
        const val NAVIGATION_TIMEOUT_MS = 5_000L
    }
}
