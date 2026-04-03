plugins {
    alias(libs.plugins.wonderland.android.library)
    alias(libs.plugins.wonderland.android.library.compose)
    alias(libs.plugins.wonderland.detekt)
}

android {
    namespace = "org.bob.cheshire.cat.feature.settings"

    defaultConfig {
        minSdk = 26
    }
}
