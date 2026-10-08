plugins {
    id("cherrypokemon.android.library")
    id("cherrypokemon.android.library.compose")
}

android {
    namespace = "com.cherryzp.cherrypokemon.core.designsystem"
}

dependencies {
    api(libs.androidx.material3)
    api(libs.androidx.ui)
    api(libs.androidx.ui.graphics)
}
