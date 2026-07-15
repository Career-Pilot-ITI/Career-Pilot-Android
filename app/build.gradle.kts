plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dagger.hilt.android)
}

android {
    namespace = "com.iti.careerpilot"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.iti.careerpilot"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // collect as state with lifecycle
    implementation(libs.androidx.lifecycle.runtime.compose)

    // datastore proto
    implementation(libs.androidx.datastore)

    // room database
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Kotlin json
    implementation(libs.kotlinx.serialization.json)

    // splash screen
    implementation(libs.androidx.core.splashscreen)

    // material3 expressive
    implementation(libs.androidx.material3.android)

    // coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor3)

    //work manager
    implementation(libs.androidx.work.runtime.ktx)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // hilt view model
    implementation(libs.androidx.hilt.navigation.compose)
    //hilt work
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    //lottie
    implementation(libs.lottie.compose)

    //kotlinx-datetime
    implementation(libs.kotlinx.datetime)

    // nav3
    implementation(libs.androidx.navigation3)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

}

dependencies{
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
}