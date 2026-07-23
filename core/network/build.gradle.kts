plugins {
    alias(libs.plugins.careerpilot.module.network)
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:datastore"))
}
