plugins {
    alias(libs.plugins.wonderland.android.library)
    alias(libs.plugins.wonderland.android.library.compose)
}

android {
    namespace = "org.bob.cheshire.cat.core.ui"
}

dependencies {
    api(libs.androidx.compose.material3)
}
