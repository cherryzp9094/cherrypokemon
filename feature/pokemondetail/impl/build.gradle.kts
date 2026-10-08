plugins {
    id("cherrypokemon.android.feature.impl")
}

android {
    namespace = "com.cherryzp.cherrypokemon.feature.pokemondetail.impl"
}

dependencies {
    implementation(projects.feature.pokemondetail.api)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.landscapist.glide)

    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
}
