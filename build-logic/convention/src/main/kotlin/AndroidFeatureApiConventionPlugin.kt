import com.cherryzp.cherrypokemon.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/** `:feature:*:api` 모듈. NavKey 와 이동 함수만 담는다. */
class AndroidFeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "cherrypokemon.android.library")
            apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

            dependencies {
                "api"(project(":core:navigation"))
                "implementation"(libs.findLibrary("kotlinx-serialization-json").get())
            }
        }
    }
}
