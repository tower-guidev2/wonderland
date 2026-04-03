plugins {
    alias(libs.plugins.wonderland.android.library)
    alias(libs.plugins.wonderland.android.library.compose)
    alias(libs.plugins.wonderland.detekt)
}

android {
    namespace = "org.bob.cheshire.cat.core.ui"

    defaultConfig {
        minSdk = 26
    }
}

dependencies {
    api(libs.androidx.compose.material3)
}
