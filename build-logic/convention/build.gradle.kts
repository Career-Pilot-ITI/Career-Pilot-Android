import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.iti.careerpilot.buildlogic"
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
            id = "careerpilot.android.application"
            implementationClass = "com.iti.careerpilot.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("careerPilotAndroidLibrary") {
            id = "careerpilot.android.library"
            implementationClass = "com.iti.careerpilot.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("careerPilotAndroidCompose") {
            id = "careerpilot.android.compose"
            implementationClass = "com.iti.careerpilot.buildlogic.AndroidComposeConventionPlugin"
        }
        register("careerPilotAndroidHilt") {
            id = "careerpilot.android.hilt"
            implementationClass = "com.iti.careerpilot.buildlogic.AndroidHiltConventionPlugin"
        }
        register("careerPilotKotlinJvm") {
            id = "careerpilot.kotlin.jvm"
            implementationClass = "com.iti.careerpilot.buildlogic.KotlinJvmConventionPlugin"
        }
        register("careerPilotKotlinSerialization") {
            id = "careerpilot.kotlin.serialization"
            implementationClass = "com.iti.careerpilot.buildlogic.KotlinSerializationConventionPlugin"
        }
        register("careerPilotAndroidRoom") {
            id = "careerpilot.android.room"
            implementationClass = "com.iti.careerpilot.buildlogic.AndroidRoomConventionPlugin"
        }
        register("careerPilotAndroidDataStore") {
            id = "careerpilot.android.datastore"
            implementationClass = "com.iti.careerpilot.buildlogic.AndroidDataStoreConventionPlugin"
        }
        register("careerPilotAndroidKtor") {
            id = "careerpilot.android.ktor"
            implementationClass = "com.iti.careerpilot.buildlogic.AndroidKtorConventionPlugin"
        }
        register("careerPilotAndroidWorkManager") {
            id = "careerpilot.android.workmanager"
            implementationClass = "com.iti.careerpilot.buildlogic.AndroidWorkManagerConventionPlugin"
        }
        register("careerPilotModuleNetwork") {
            id = "careerpilot.module.network"
            implementationClass = "com.iti.careerpilot.buildlogic.NetworkModuleConventionPlugin"
        }
        register("careerPilotModuleDatabase") {
            id = "careerpilot.module.database"
            implementationClass = "com.iti.careerpilot.buildlogic.DatabaseModuleConventionPlugin"
        }
        register("careerPilotModuleDataStore") {
            id = "careerpilot.module.datastore"
            implementationClass = "com.iti.careerpilot.buildlogic.DataStoreModuleConventionPlugin"
        }
        register("careerPilotModuleModels") {
            id = "careerpilot.module.models"
            implementationClass = "com.iti.careerpilot.buildlogic.ModelsModuleConventionPlugin"
        }
        register("careerPilotFeatureDomain") {
            id = "careerpilot.feature.domain"
            implementationClass = "com.iti.careerpilot.buildlogic.FeatureDomainConventionPlugin"
        }
        register("careerPilotFeatureData") {
            id = "careerpilot.feature.data"
            implementationClass = "com.iti.careerpilot.buildlogic.FeatureDataConventionPlugin"
        }
        register("careerPilotFeaturePresentation") {
            id = "careerpilot.feature.presentation"
            implementationClass = "com.iti.careerpilot.buildlogic.FeaturePresentationConventionPlugin"
        }
    }
}

tasks.validatePlugins {
    enableStricterValidation.set(true)
    failOnWarning.set(true)
}
