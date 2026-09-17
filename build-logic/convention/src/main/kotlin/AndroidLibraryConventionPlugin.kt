import com.android.build.api.dsl.LibraryExtension
import com.cherryzp.cherrypokemon.TARGET_SDK
import com.cherryzp.cherrypokemon.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.library")
            apply(plugin = "org.jetbrains.kotlin.android")

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                testOptions.targetSdk = TARGET_SDK
                lint.targetSdk = TARGET_SDK
                // 모듈 경로로 리소스 이름 접두사를 정한다. (:core:ui → core_ui_)
                resourcePrefix = path.split("""\W""".toRegex())
                    .drop(1)
                    .distinct()
                    .joinToString(separator = "_")
                    .lowercase() + "_"
            }
        }
    }
}
