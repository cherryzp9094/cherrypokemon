plugins {
    id("cherrypokemon.android.library")
    id("cherrypokemon.hilt")
    alias(libs.plugins.kotlin.parcelize)
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
