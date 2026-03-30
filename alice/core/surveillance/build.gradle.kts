plugins {
    alias(libs.plugins.wonderland.android.library)
}

android {
    namespace = "org.alice.rabbit.hole.core.surveillance"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    api(projects.alice.core.surveillanceApi)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.android)
    implementation(libs.koin.core)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.bouncycastle.bcprov)

    testImplementation(libs.assertk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.koin.test)
    testImplementation(libs.androidx.test.core)
}
