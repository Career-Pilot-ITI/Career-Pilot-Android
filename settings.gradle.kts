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
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Career Pilot"

include(":app")
include(":core:common")
include(":core:designsystem")
include(":core:network")
include(":core:model")
include(":core:database")
include(":core:datastore")
include(":feature:login")
include(":feature:onboarding")
include(":feature:profile")
include(":feature:editprofile")
include(":feature:practicesession")
include(":core:whisper")
