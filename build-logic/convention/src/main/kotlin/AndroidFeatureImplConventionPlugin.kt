import com.cherryzp.cherrypokemon.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/** `:feature:*:impl` 모듈. 화면과 ViewModel, entry builder 를 담는다. */
class AndroidFeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "cherrypokemon.android.library")
            apply(plugin = "cherrypokemon.android.library.compose")
            apply(plugin = "cherrypokemon.hilt")

            dependencies {
                "implementation"(project(":core:ui"))
                "implementation"(project(":core:designsystem"))
                "implementation"(project(":core:data"))

                "implementation"(libs.findLibrary("androidx-navigation3-runtime").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel-navigation3").get())
                "implementation"(
                    libs.findLibrary("androidx-hilt-lifecycle-viewmodel-compose").get()
                )
                "implementation"(libs.findLibrary("androidx-lifecycle-runtime-ktx").get())

                "implementation"(libs.findLibrary("orbit-viewmodel").get())
                "implementation"(libs.findLibrary("orbit-compose").get())

                "testImplementation"(project(":core:testing"))
                "testImplementation"(libs.findLibrary("orbit-test").get())
            }
        }
    }
}
