plugins {
    id("synco.android.library")
}

android {
    namespace = "app.synco.shizuku"
}

dependencies {
    api(project(":core:logging"))
    api(project(":core:transfer"))
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.hidden.api.bypass)
}
