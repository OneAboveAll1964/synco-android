plugins {
    id("synco.android.library")
}

android {
    namespace = "app.synco.service"
}

dependencies {
    api(project(":sync"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.startup.runtime)
}
