plugins {
    id("cherrypokemon.android.application")
    id("cherrypokemon.android.application.compose")
    id("cherrypokemon.hilt")
    id(libs.plugins.kotlin.parcelize.get().pluginId)
}

android {
    namespace = "com.cherryzp.cherrypokemon"

    defaultConfig {
        applicationId = "com.cherryzp.cherrypokemon"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":data"))
    implementation(project(":domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.palette.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.material)

    implementation(libs.bundles.paging)
    implementation(libs.room.paging)

    implementation(libs.kotlinx.collections.immutable)

    implementation(libs.hilt.navigation)

    implementation(libs.glide)
}
