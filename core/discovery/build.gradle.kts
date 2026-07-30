plugins {
    id("synco.android.library")
}

android {
    namespace = "app.synco.discovery"
}

dependencies {
    api(project(":core:protocol"))
    implementation(libs.androidx.core.ktx)
}
