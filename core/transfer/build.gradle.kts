plugins {
    id("synco.android.library")
}

android {
    namespace = "app.synco.transfer"
}

dependencies {
    api(project(":core:protocol"))
    api(project(":core:logging"))
    implementation(libs.androidx.core.ktx)
}
