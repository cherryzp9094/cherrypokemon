plugins {
    id("cherrypokemon.android.library")
    id("cherrypokemon.android.room")
    id("cherrypokemon.hilt")
}

android {
    namespace = "com.cherryzp.cherrypokemon.core.database"
}

dependencies {
    api(projects.core.model)

    api(libs.paging.common.ktx)
    api(libs.room.paging)

    androidTestImplementation(projects.core.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
