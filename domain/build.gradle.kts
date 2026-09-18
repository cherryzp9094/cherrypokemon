plugins {
    id("cherrypokemon.android.library")
    id("cherrypokemon.hilt")
}

android {
    namespace = "com.cherryzp.domain"
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.bundles.paging)
}
