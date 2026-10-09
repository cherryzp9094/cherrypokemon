plugins {
    id("cherrypokemon.android.library")
    id("cherrypokemon.hilt")
}

android {
    namespace = "com.cherryzp.cherrypokemon.core.testing"
}

dependencies {
    api(projects.core.data)
    api(projects.core.model)
    api(libs.paging.common.ktx)
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.androidx.test.runner)
    api(libs.hilt.android.testing)
}
