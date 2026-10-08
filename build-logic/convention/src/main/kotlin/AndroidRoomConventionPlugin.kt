import androidx.room3.gradle.RoomExtension
import com.cherryzp.cherrypokemon.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "androidx.room3")
            apply(plugin = "com.google.devtools.ksp")

            extensions.configure<RoomExtension> {
                // 스키마 JSON 을 git 에 커밋해 마이그레이션을 검증할 수 있게 한다.
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                "implementation"(libs.findLibrary("room-runtime").get())
                "ksp"(libs.findLibrary("room-compiler").get())
            }
        }
    }
}
