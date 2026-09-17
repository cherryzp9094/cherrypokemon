package com.cherryzp.cherrypokemon

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/** 모든 Android 모듈에 공통인 SDK·Java·Kotlin 설정. SDK와 언어 버전은 여기서만 정한다. */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        compileSdk = COMPILE_SDK

        defaultConfig {
            minSdk = MIN_SDK
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            sourceCompatibility = JAVA_VERSION
            targetCompatibility = JAVA_VERSION
        }
    }

    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(JAVA_VERSION.toString()))
        }
    }
}

internal const val COMPILE_SDK = 34
internal const val MIN_SDK = 26
internal const val TARGET_SDK = 34
private val JAVA_VERSION = JavaVersion.VERSION_17
