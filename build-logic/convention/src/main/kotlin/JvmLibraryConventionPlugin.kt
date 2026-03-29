import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import wonderland.configureKotlinJvm
import wonderland.libs

abstract class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "org.jetbrains.kotlin.jvm")
            apply(plugin = "wonderland.android.lint")

            configureKotlinJvm()
            dependencies {
                "testImplementation"(libs.findLibrary("junit").get())
            }
        }
    }
}
