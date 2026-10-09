plugins {
    id("cherrypokemon.android.library")
    id("cherrypokemon.android.library.compose")
    alias(libs.plugins.screenshot)
}

android {
    namespace = "com.cherryzp.cherrypokemon.core.ui"

    experimentalProperties["android.experimental.enableScreenshotTest"] = true
}

dependencies {
    api(projects.core.designsystem)
    api(projects.core.model)

    implementation(libs.landscapist.glide)
    implementation(libs.landscapist.palette)

    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)

    screenshotTestImplementation(libs.androidx.ui.tooling)
    screenshotTestImplementation(libs.screenshot.validation.api)
}
