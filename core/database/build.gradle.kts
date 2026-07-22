plugins {
    alias(libs.plugins.careerpilot.module.database)
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
}
