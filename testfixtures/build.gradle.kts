@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.wonderland.android.library)
    alias(libs.plugins.wonderland.detekt)
}

android {
    namespace = "org.alice.rabbit.hole.testfixtures"

    testFixtures {
        enable = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    testFixturesApi(libs.junit)
    testFixturesApi(libs.assertk.jvm)
    testFixturesApi(libs.kotlinx.coroutines.test)
    testFixturesApi(libs.kotlin.faker)
}
