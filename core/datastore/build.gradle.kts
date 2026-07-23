plugins {
    alias(libs.plugins.careerpilot.module.datastore)
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
}
