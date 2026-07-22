plugins {
    alias(libs.plugins.careerpilot.module.app)
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:login"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:editprofile"))
    implementation(project(":feature:practicesession"))
}
