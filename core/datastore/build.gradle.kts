plugins {
    id("cherrypokemon.android.library")
    id("cherrypokemon.hilt")
}

android {
    namespace = "com.cherryzp.cherrypokemon.core.datastore"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.common)

    implementation(libs.androidx.datastore.preferences)

    testImplementation(projects.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
}
