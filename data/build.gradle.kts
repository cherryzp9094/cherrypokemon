plugins {
    id("cherrypokemon.android.library")
    id("cherrypokemon.hilt")
}

android {
    namespace = "com.cherryzp.data"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.bundles.paging)

    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    ksp(libs.room.compiler)
    implementation(libs.room.paging)
    implementation(libs.room.ktx)

    implementation(libs.bundles.retrofit)
}
