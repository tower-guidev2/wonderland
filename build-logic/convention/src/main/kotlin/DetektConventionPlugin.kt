import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import wonderland.libs

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.gitlab.arturbosch.detekt")

            extensions.configure<DetektExtension> {
                buildUponDefaultConfig = true
                allRules = false
                parallel = true
                autoCorrect = false
                config.setFrom("${rootProject.projectDir}/config/detekt/detekt.yml")
            }

            dependencies {
                "detektPlugins"(libs.findLibrary("detekt-ktlint-wrapper").get())
            }

            pluginManager.withPlugin("org.jetbrains.kotlin.plugin.compose") {
                dependencies {
                    "detektPlugins"(libs.findLibrary("detekt-compose-rules").get())
                }
            }

            tasks.withType<Detekt>().configureEach {
                jvmTarget = "21"
                autoCorrect = false
                reports {
                    html.required.set(true)
                    sarif.required.set(true)
                    md.required.set(true)
                }
            }

            tasks.withType<DetektCreateBaselineTask>().configureEach {
                jvmTarget = "21"
            }
        }
    }
}
