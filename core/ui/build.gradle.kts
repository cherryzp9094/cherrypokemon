plugins {
    id("cherrypokemon.android.library")
    id("cherrypokemon.android.library.compose")
}

android {
    namespace = "com.cherryzp.cherrypokemon.core.ui"
}

dependencies {
    api(projects.core.designsystem)
    api(projects.core.model)

    implementation(libs.landscapist.glide)
    implementation(libs.landscapist.palette)

    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
}
