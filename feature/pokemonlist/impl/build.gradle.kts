plugins {
    id("cherrypokemon.android.feature.impl")
}

android {
    namespace = "com.cherryzp.cherrypokemon.feature.pokemonlist.impl"
}

dependencies {
    implementation(projects.feature.pokemonlist.api)

    implementation(libs.paging.compose)
    testImplementation(libs.paging.testing)

    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
}
