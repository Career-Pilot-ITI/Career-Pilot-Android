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
        register("careerPilotModuleApp") {
            id = libs.plugins.careerpilot.module.app.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.modules.AppModuleConventionPlugin"
        }
        register("careerPilotModuleCommon") {
            id = libs.plugins.careerpilot.module.common.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.modules.CommonModuleConventionPlugin"
        }
        register("careerPilotModuleDesignSystem") {
            id = libs.plugins.careerpilot.module.designsystem.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.modules.DesignSystemModuleConventionPlugin"
        }
        register("careerPilotModuleNetwork") {
            id = libs.plugins.careerpilot.module.network.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.modules.NetworkModuleConventionPlugin"
        }
        register("careerPilotModuleDatabase") {
            id = libs.plugins.careerpilot.module.database.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.modules.DatabaseModuleConventionPlugin"
        }
        register("careerPilotModuleDataStore") {
            id = libs.plugins.careerpilot.module.datastore.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.modules.DataStoreModuleConventionPlugin"
        }
        register("careerPilotModuleModel") {
            id = libs.plugins.careerpilot.module.model.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.modules.ModelModuleConventionPlugin"
        }
        register("careerPilotModuleWhisper") {
            id = libs.plugins.careerpilot.module.whisper.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.modules.WhisperModuleConventionPlugin"
        }
        register("careerPilotFeatureLogin") {
            id = libs.plugins.careerpilot.feature.login.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.features.LoginFeatureConventionPlugin"
        }
        register("careerPilotFeatureOnboarding") {
            id = libs.plugins.careerpilot.feature.onboarding.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.features.OnboardingFeatureConventionPlugin"
        }
        register("careerPilotFeatureProfile") {
            id = libs.plugins.careerpilot.feature.profile.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.features.ProfileFeatureConventionPlugin"
        }
        register("careerPilotFeatureEditProfile") {
            id = libs.plugins.careerpilot.feature.editprofile.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.features.EditProfileFeatureConventionPlugin"
        }
        register("careerPilotFeaturePracticeSession") {
            id = libs.plugins.careerpilot.feature.practice.session.get().pluginId
            implementationClass = "com.iti.careerpilot.buildlogic.features.PracticeSessionConventionPlugin"
        }
    }
}

tasks.validatePlugins {
    enableStricterValidation.set(true)
    failOnWarning.set(true)
}
