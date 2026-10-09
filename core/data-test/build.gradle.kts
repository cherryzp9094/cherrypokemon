plugins {
    id("cherrypokemon.android.library")
    id("cherrypokemon.hilt")
}

android {
    namespace = "com.cherryzp.cherrypokemon.core.data.test"
}

dependencies {
    api(projects.core.data)
    api(projects.core.testing)
}
