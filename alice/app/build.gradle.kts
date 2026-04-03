plugins {
    alias(libs.plugins.wonderland.android.application)
    alias(libs.plugins.wonderland.android.application.compose)
    alias(libs.plugins.wonderland.detekt)
}

android {
    namespace = "org.alice.rabbit.hole"

    defaultConfig {
        applicationId = "org.alice.rabbit.hole"
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
    }
}

dependencies {
    implementation(projects.core.cryptography)
    implementation(projects.alice.core.surveillance)
    implementation(libs.koin.androidx.startup)
}
