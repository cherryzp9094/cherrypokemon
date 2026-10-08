plugins {
    id("cherrypokemon.android.library")
    id("cherrypokemon.hilt")
}

android {
    namespace = "com.cherryzp.cherrypokemon.core.data"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.database)
    implementation(projects.core.network)

    implementation(libs.paging.runtime)
    // RemoteMediator 가 HTTP 오류를 MediatorResult.Error 로 바꾸려면 필요하다.
    implementation(libs.retrofit)

    testImplementation(projects.core.testing)
    testImplementation(libs.paging.testing)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(projects.core.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
