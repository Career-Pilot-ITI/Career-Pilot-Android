plugins {
    alias(libs.plugins.careerpilot.android.application)
    alias(libs.plugins.careerpilot.android.compose)
    alias(libs.plugins.careerpilot.android.hilt)
    alias(libs.plugins.careerpilot.kotlin.serialization)
    alias(libs.plugins.careerpilot.android.room)
    alias(libs.plugins.careerpilot.android.datastore)
    alias(libs.plugins.careerpilot.android.workmanager)
    alias(libs.plugins.careerpilot.testing)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.iti.careerpilot"

    defaultConfig {
        applicationId = "com.iti.careerpilot"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    androidResources {
        noCompress.add("task")
    }
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.material3.android)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor3)
    implementation(libs.lottie.compose)
    implementation(libs.kotlinx.datetime)

    implementation(libs.androidx.navigation3)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.animated.navigation.bar)

    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:access"))
    implementation(project(":core:model"))
    implementation(project(":feature:payment"))
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:login"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:editprofile"))
    implementation(project(":feature:home"))
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.appcheck.debug)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(project(":feature:settings"))
    implementation(project(":feature:practicesession"))
    implementation(project(":feature:quiz"))
    implementation(project(":feature:reports"))
}
