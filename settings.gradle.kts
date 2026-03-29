pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "wonderland"

include(":app")

// Test fixtures
include(":testfixtures")

// Core
include(":core:common")
include(":core:protocol")
include(":core:cryptography")
include(":core:qr")

// Alice — D1 Vault (air-gapped, minSdk 33)
include(":alice:app")
include(":alice:core:surveillance")
include(":alice:core:ui")
include(":alice:feature:contacts")
include(":alice:feature:keygen")
include(":alice:feature:messaging")
include(":alice:feature:pairing")
include(":alice:feature:scanner")
include(":alice:feature:settings")
include(":alice:feature:delivery")

// Bob — D2 Courier (online, minSdk 26)
include(":bob:app")
include(":bob:core:ui")
include(":bob:feature:scanner")
include(":bob:feature:contacts")
include(":bob:feature:delivery")
include(":bob:feature:receive")
include(":bob:feature:pairing")
include(":bob:feature:settings")
