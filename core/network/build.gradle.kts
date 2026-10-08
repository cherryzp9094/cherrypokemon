plugins {
    id("cherrypokemon.android.library")
    id("cherrypokemon.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.cherryzp.cherrypokemon.core.network"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "POKE_API_BASE_URL", "\"https://pokeapi.co/api/v2/\"")
    }
}

dependencies {
    implementation(projects.core.common)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp.logging)

    testImplementation(projects.core.testing)
}
