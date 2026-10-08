import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class AndroidLintConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            when {
                pluginManager.hasPlugin("com.android.application") ->
                    extensions.configure<ApplicationExtension> { lint { configure(project) } }

                pluginManager.hasPlugin("com.android.library") ->
                    extensions.configure<LibraryExtension> { lint { configure(project) } }

                else -> {
                    apply(plugin = "com.android.lint")
                    extensions.configure<Lint> { configure(project) }
                }
            }
        }
    }
}

/**
 * 의존하는 모듈까지 함께 검사하고, 지금 코드의 이슈는 baseline 으로 넘긴다.
 * baseline 에 새 이슈를 추가하지 않는다. 옛 코드를 지울 때 baseline 도 같이 줄인다.
 */
private fun Lint.configure(project: Project) {
    checkDependencies = true
    xmlReport = true
    baseline = project.file("lint-baseline.xml")
}
