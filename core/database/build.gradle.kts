plugins {
    alias(libs.plugins.careerpilot.module.database)
}

android {
    namespace = "com.iti.core.database"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
}