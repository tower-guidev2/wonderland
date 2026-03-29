plugins {
    alias(libs.plugins.wonderland.android.library)
    alias(libs.plugins.wonderland.android.library.compose)
}

android {
    namespace = "org.alice.rabbit.hole.core.ui"
}

dependencies {
    api(libs.androidx.compose.material3)
}
