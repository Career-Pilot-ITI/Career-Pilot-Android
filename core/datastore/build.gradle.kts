plugins {
    alias(libs.plugins.careerpilot.module.datastore)
    alias(libs.plugins.careerpilot.testing)
}

android {
    namespace = "com.iti.core.datastore"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
}