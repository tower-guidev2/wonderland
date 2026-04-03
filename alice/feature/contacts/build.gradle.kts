plugins {
    alias(libs.plugins.wonderland.android.library)
    alias(libs.plugins.wonderland.android.library.compose)
    alias(libs.plugins.wonderland.detekt)
}

android {
    namespace = "org.alice.rabbit.hole.feature.contacts"
}
