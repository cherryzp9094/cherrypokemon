package com.cherryzp.cherrypokemon

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

/** Compose를 쓰는 Android 모듈의 공통 설정. */
internal fun Project.configureAndroidCompose(commonExtension: CommonExtension) {
    commonExtension.apply {
        buildFeatures.compose = true
    }

    dependencies {
        val bom = libs.findLibrary("androidx-compose-bom").get()
        "implementation"(platform(bom))
        "androidTestImplementation"(platform(bom))
        "implementation"(libs.findLibrary("androidx-ui-tooling-preview").get())
        "debugImplementation"(libs.findLibrary("androidx-ui-tooling").get())
    }

    extensions.configure<ComposeCompilerGradlePluginExtension> {
        // 다른 모듈의 모델을 안정적으로 본다. 내용이 같은 새 객체가 오면 리컴포지션을 건너뛴다.
        stabilityConfigurationFiles.add(
            rootProject.layout.projectDirectory.file("compose_compiler_config.conf")
        )

        // 리컴포지션을 진단할 때만 켠다: ./gradlew assembleRelease -PenableComposeCompilerReports=true
        // 결과는 각 모듈의 build/compose-reports 에 생긴다.
        val reportsEnabled = providers.gradleProperty("enableComposeCompilerReports")
            .map(String::toBoolean)
            .getOrElse(false)
        if (reportsEnabled) {
            reportsDestination.set(layout.buildDirectory.dir("compose-reports"))
            metricsDestination.set(layout.buildDirectory.dir("compose-reports"))
        }
    }
}
