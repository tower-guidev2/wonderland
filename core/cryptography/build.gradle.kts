plugins {
    alias(libs.plugins.wonderland.jvm.library)
    alias(libs.plugins.wonderland.detekt)
}

dependencies {
    api(libs.bouncycastle.bcprov)
    implementation(projects.core.protocol)
}
