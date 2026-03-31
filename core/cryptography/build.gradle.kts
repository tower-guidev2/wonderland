plugins {
    alias(libs.plugins.wonderland.jvm.library)
}

dependencies {
    api(libs.bouncycastle.bcprov)
    implementation(projects.core.protocol)
}
