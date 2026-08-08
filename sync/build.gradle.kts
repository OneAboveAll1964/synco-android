plugins {
    id("synco.android.library")
    id("synco.serialization")
}

android {
    namespace = "app.synco.sync"
}

dependencies {
    api(project(":core:protocol"))
    api(project(":core:crypto"))
    api(project(":core:transport"))
    api(project(":core:discovery"))
    api(project(":core:clipboard"))
    api(project(":core:storage"))
    api(project(":core:transfer"))
    api(project(":core:remote"))
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.turbine)
}
