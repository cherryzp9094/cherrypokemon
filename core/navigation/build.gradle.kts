plugins {
    id("cherrypokemon.android.library")
    id("cherrypokemon.android.library.compose")
}

android {
    namespace = "com.cherryzp.cherrypokemon.core.navigation"
}

dependencies {
    api(libs.androidx.navigation3.runtime)
}
