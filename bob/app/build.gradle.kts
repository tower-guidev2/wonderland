plugins {
    alias(libs.plugins.wonderland.android.application)
    alias(libs.plugins.wonderland.android.application.compose)
    alias(libs.plugins.wonderland.detekt)
}

android {
    namespace = "org.bob.cheshire.cat"

    defaultConfig {
        applicationId = "org.bob.cheshire.cat"
        minSdk = 26
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
    }
}
