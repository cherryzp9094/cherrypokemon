import com.android.build.api.dsl.LibraryExtension
import com.cherryzp.cherrypokemon.TARGET_SDK
import com.cherryzp.cherrypokemon.configureGradleManagedDevices
import com.cherryzp.cherrypokemon.configureKotlinAndroid
import com.cherryzp.cherrypokemon.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.library")
            apply(plugin = "cherrypokemon.android.lint")

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                configureGradleManagedDevices(this)
                testOptions.targetSdk = TARGET_SDK
                lint.targetSdk = TARGET_SDK
                // 모듈 경로로 리소스 이름 접두사를 정한다. (:core:ui → core_ui_)
                resourcePrefix = path.split("""\W""".toRegex())
                    .drop(1)
                    .distinct()
                    .joinToString(separator = "_")
                    .lowercase() + "_"
            }

            dependencies {
                // 계측 테스트 공통 의존성. 모듈마다 선언하면 Compose 테스트가 끌어오는
                // 옛 Espresso 가 이겨서 최신 기기에서 깨진다.
                "androidTestImplementation"(libs.findLibrary("androidx-junit").get())
                "androidTestImplementation"(libs.findLibrary("androidx-test-runner").get())
                "androidTestImplementation"(libs.findLibrary("androidx-espresso-core").get())
            }
        }
    }
}
