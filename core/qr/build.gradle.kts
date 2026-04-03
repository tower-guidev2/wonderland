plugins {
    alias(libs.plugins.wonderland.jvm.library)
    alias(libs.plugins.wonderland.detekt)
}

dependencies {
    implementation(libs.zxing.core)
}
