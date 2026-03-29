plugins {
    alias(libs.plugins.wonderland.jvm.library)
}

dependencies {
    implementation(libs.bouncycastle.bcprov)
    implementation(projects.core.protocol)
}
