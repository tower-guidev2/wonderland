plugins {
    alias(libs.plugins.wonderland.android.library)
    alias(libs.plugins.wonderland.android.library.compose)
}

android {
    namespace = "org.bob.cheshire.cat.feature.contacts"

    defaultConfig {
        minSdk = 26
    }
}
