package com.cherryzp.cherrypokemon

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.ManagedVirtualDevice
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.invoke

/**
 * CI 에서 계측 테스트를 돌릴 가상 기기.
 * ATD(Automated Test Device) 는 화면이 없는 테스트 전용 이미지라 더 빠르다.
 */
internal fun configureGradleManagedDevices(commonExtension: CommonExtension) {
    commonExtension.testOptions.managedDevices.allDevices {
        create<ManagedVirtualDevice>("pixel5Api35") {
            device = "Pixel 5"
            apiLevel = 35
            systemImageSource = "aosp-atd"
        }
    }
}
