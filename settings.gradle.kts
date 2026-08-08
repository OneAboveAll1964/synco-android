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

rootProject.name = "synco"

include(":app")
include(":core:logging")
include(":core:shizuku")
include(":core:protocol")
include(":core:remote")
include(":core:crypto")
include(":core:transport")
include(":core:discovery")
include(":core:clipboard")
include(":core:storage")
include(":core:transfer")
include(":sync")
include(":service")
