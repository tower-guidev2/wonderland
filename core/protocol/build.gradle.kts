plugins {
    alias(libs.plugins.wonderland.jvm.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.wonderland.detekt)
}

dependencies {
    implementation(libs.kotlinx.serialization.cbor)
}
