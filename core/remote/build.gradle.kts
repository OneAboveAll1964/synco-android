plugins {
    id("synco.android.library")
}

android {
    namespace = "app.synco.remote"
}

dependencies {
    api(project(":core:protocol"))
    implementation(project(":core:logging"))
    testImplementation(libs.junit)
}
