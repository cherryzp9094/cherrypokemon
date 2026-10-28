plugins {
    id("cherrypokemon.android.feature.impl")
    alias(libs.plugins.screenshot)
}

android {
    namespace = "com.cherryzp.cherrypokemon.feature.pokemonlist.impl"

    experimentalProperties["android.experimental.enableScreenshotTest"] = true
}

dependencies {
    implementation(projects.feature.pokemonlist.api)

    implementation(libs.androidx.material.icons.core)
    implementation(libs.paging.compose)
    testImplementation(libs.paging.testing)

    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)

    screenshotTestImplementation(libs.androidx.ui.tooling)
    screenshotTestImplementation(libs.screenshot.validation.api)
}
