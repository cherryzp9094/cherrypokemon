plugins {
    id("cherrypokemon.android.library")
    id("cherrypokemon.hilt")
    id(libs.plugins.kotlin.parcelize.get().pluginId)
}

android {
    namespace = "com.cherryzp.domain"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.bundles.paging)
}
