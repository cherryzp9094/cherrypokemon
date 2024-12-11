package com.cherryzp.cherrypokemon.app

import com.android.build.gradle.BaseExtension

fun BaseExtension.setConfigs() {
    compileSdkVersion(Versions.COMPILE_SDK)

    defaultConfig {
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK

        vectorDrawables { useSupportLibrary = true }
    }
}

fun BaseExtension.setBuildType() {
    buildTypes {
        getByName(BuildTask.DEBUG) {
            isDebuggable = BuildTaskDebug.isDebuggable
            isMinifyEnabled = BuildTaskDebug.isMinifyEnabled
        }
        getByName(BuildTask.RELEASE) {
            isDebuggable = BuildTaskRelease.isDebuggable
            isMinifyEnabled = BuildTaskRelease.isMinifyEnabled
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
