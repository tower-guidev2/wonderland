plugins {
    alias(libs.plugins.wonderland.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.cbor)
}
