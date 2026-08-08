plugins {
    id("synco.android.library")
}

android {
    namespace = "app.synco.remote"
}

dependencies {
    api(project(":core:protocol"))
    testImplementation(libs.junit)
}
