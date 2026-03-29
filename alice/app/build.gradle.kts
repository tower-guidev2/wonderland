plugins {
    alias(libs.plugins.wonderland.android.application)
    alias(libs.plugins.wonderland.android.application.compose)
}

android {
    namespace = "org.alice.rabbit.hole"

    defaultConfig {
        applicationId = "org.alice.rabbit.hole"
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
    }
}
