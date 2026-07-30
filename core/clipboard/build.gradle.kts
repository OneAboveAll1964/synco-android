plugins {
    id("synco.android.library")
}

android {
    namespace = "app.synco.clipboard"
}

dependencies {
    api(project(":core:protocol"))
    api(project(":core:transfer"))
    implementation(libs.androidx.core.ktx)
}
