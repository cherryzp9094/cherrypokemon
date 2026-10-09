import com.android.build.api.dsl.ApplicationExtension
import com.cherryzp.cherrypokemon.TARGET_SDK
import com.cherryzp.cherrypokemon.configureGradleManagedDevices
import com.cherryzp.cherrypokemon.configureKotlinAndroid
import com.cherryzp.cherrypokemon.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.application")
            apply(plugin = "cherrypokemon.android.lint")

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                configureGradleManagedDevices(this)
                defaultConfig.targetSdk = TARGET_SDK

                buildTypes {
                    release {
                        // AGP 9.3 이상의 최적화 DSL. keep 규칙은 src/<variant>/keepRules/*.keep 에 둔다.
                        optimization {
                            enable = true
                        }
                    }
                }

                // 번역이 있는 언어만 담는다. 다른 언어의 기본 리소스는 빼서 용량을 줄인다.
                androidResources {
                    localeFilters += listOf("en", "ko")
                }

                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }
            }

            dependencies {
                // 계측 테스트 공통 의존성. 라이브러리 plugin 과 같은 이유로 여기서 건다.
                "androidTestImplementation"(libs.findLibrary("androidx-junit").get())
                "androidTestImplementation"(libs.findLibrary("androidx-test-runner").get())
                "androidTestImplementation"(libs.findLibrary("androidx-espresso-core").get())
            }
        }
    }
}
