plugins {
    id("cherrypokemon.android.library")
}

android {
    namespace = "com.cherryzp.cherrypokemon.core.testing"
}

dependencies {
    api(projects.core.model)
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
}
