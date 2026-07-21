import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.iti.careerpilot.buildlogic"

/*
 * Do not request an exact Java toolchain here.
 *
 * AGP 9.2.1 already requires Gradle to run on JDK 17 or newer. Compiling the
 * convention plugins with the current Gradle JVM avoids forcing developers to
 * install a separate JDK 17 when Android Studio is already running Gradle with
 * its embedded JDK 21.
 */
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.compose.gradle.plugin)
    implementation(libs.kotlin.serialization.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation(libs.hilt.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("careerPilotAndroidApplication") {
            id = libs.plugins.careerpilot.android.application.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("careerPilotAndroidLibrary") {
            id = libs.plugins.careerpilot.android.library.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("careerPilotAndroidCompose") {
            id = libs.plugins.careerpilot.android.compose.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.AndroidComposeConventionPlugin"
        }
        register("careerPilotAndroidHilt") {
            id = libs.plugins.careerpilot.android.hilt.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.AndroidHiltConventionPlugin"
        }
        register("careerPilotKotlinJvm") {
            id = libs.plugins.careerpilot.kotlin.jvm.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.KotlinJvmConventionPlugin"
        }
        register("careerPilotKotlinSerialization") {
            id = libs.plugins.careerpilot.kotlin.serialization.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.KotlinSerializationConventionPlugin"
        }
        register("careerPilotAndroidRoom") {
            id = libs.plugins.careerpilot.android.room.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.AndroidRoomConventionPlugin"
        }
        register("careerPilotAndroidDataStore") {
            id = libs.plugins.careerpilot.android.datastore.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.AndroidDataStoreConventionPlugin"
        }
        register("careerPilotAndroidKtor") {
            id = libs.plugins.careerpilot.android.ktor.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.AndroidKtorConventionPlugin"
        }
        register("careerPilotAndroidWorkManager") {
            id = libs.plugins.careerpilot.android.workmanager.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.AndroidWorkManagerConventionPlugin"
        }
        register("careerPilotModuleNetwork") {
            id = libs.plugins.careerpilot.module.network.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.NetworkModuleConventionPlugin"
        }
        register("careerPilotModuleDatabase") {
            id = libs.plugins.careerpilot.module.database.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.DatabaseModuleConventionPlugin"
        }
        register("careerPilotModuleDataStore") {
            id = libs.plugins.careerpilot.module.datastore.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.DataStoreModuleConventionPlugin"
        }
        register("careerPilotModuleModels") {
            id = libs.plugins.careerpilot.module.models.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.ModelsModuleConventionPlugin"
        }
        register("careerPilotFeatureDomain") {
            id = libs.plugins.careerpilot.feature.domain.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.FeatureDomainConventionPlugin"
        }
        register("careerPilotFeatureData") {
            id = libs.plugins.careerpilot.feature.data.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.FeatureDataConventionPlugin"
        }
        register("careerPilotFeaturePresentation") {
            id = libs.plugins.careerpilot.feature.presentation.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.FeaturePresentationConventionPlugin"
        }
        register("careerPilotFeatureProfile") {
            id = libs.plugins.careerpilot.feature.profile.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.ProfileConventionPlugin"
        }
        register("careerPilotFeaturePracticeSession") {
            id = libs.plugins.careerpilot.feature.practice.session.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.PracticeSessionConventionPlugin"
        }
        register("careerPilotTesting") {
            id = libs.plugins.careerpilot.testing.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.TestingConventionPlugin"
        }

        // Features
        register("onboardingFeature") {
            id = "feature.onboarding"
            implementationClass = "com.iti.careerpilot.buildlogic.OnboardingConventionPlugin"
        }
    }
}

tasks.validatePlugins {
    enableStricterValidation.set(true)
    failOnWarning.set(true)
}
