plugins {
    id("cherrypokemon.android.feature.impl")
    alias(libs.plugins.screenshot)
}

android {
    namespace = "com.cherryzp.cherrypokemon.feature.pokemondetail.impl"

    experimentalProperties["android.experimental.enableScreenshotTest"] = true
}

dependencies {
    implementation(projects.feature.pokemondetail.api)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.landscapist.glide)

    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)

    screenshotTestImplementation(libs.androidx.ui.tooling)
    screenshotTestImplementation(libs.screenshot.validation.api)
}
