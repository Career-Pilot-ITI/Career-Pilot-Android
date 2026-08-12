buildscript {
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
    }
}
plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.google.services) apply false
}
// Shared build configuration is implemented in the included build under build-logic/.
// Keep this root build file intentionally small.
