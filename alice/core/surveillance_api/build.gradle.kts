plugins {
    alias(libs.plugins.wonderland.android.library)
}

android {
    namespace = "org.alice.rabbit.hole.core.surveillance.api"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.assertk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
}
