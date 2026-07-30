plugins {
    id("synco.android.library")
    id("synco.serialization")
}

android {
    namespace = "app.synco.storage"
}

dependencies {
    api(project(":core:protocol"))
    api(project(":core:crypto"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)
}
