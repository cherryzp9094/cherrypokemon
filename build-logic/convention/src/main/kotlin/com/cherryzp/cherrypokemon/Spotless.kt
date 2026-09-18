package com.cherryzp.cherrypokemon

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** 모든 모듈에 Spotless + ktlint(Android 스타일)를 적용한다. */
internal fun Project.configureSpotless() {
    pluginManager.apply("com.diffplug.spotless")

    extensions.configure<SpotlessExtension> {
        val ktlintVersion = libs.findVersion("ktlint").get().toString()

        kotlin {
            target("src/**/*.kt")
            targetExclude("**/build/**/*.kt")
            ktlint(ktlintVersion).editorConfigOverride(mapOf("android" to "true"))
        }
    }
}
