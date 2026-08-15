plugins {
    alias(libs.plugins.careerpilot.module.datastore)
}

android {
    namespace = "com.iti.core.datastore"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))

    testImplementation(libs.junit)
}