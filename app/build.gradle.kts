plugins {
    id("cherrypokemon.android.application")
    id("cherrypokemon.android.application.compose")
    id("cherrypokemon.hilt")
}

android {
    namespace = "com.cherryzp.cherrypokemon"

    defaultConfig {
        applicationId = "com.cherryzp.cherrypokemon"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "com.cherryzp.cherrypokemon.core.testing.util.CherryPokemonTestRunner"
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.navigation)
    implementation(projects.feature.pokemondetail.api)
    implementation(projects.feature.pokemondetail.impl)
    implementation(projects.feature.pokemonlist.api)
    implementation(projects.feature.pokemonlist.impl)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.ui)

    testImplementation(libs.junit)

    androidTestImplementation(projects.core.dataTest)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.androidx.ui.test.manifest)
}
