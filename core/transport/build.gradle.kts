plugins {
    id("synco.jvm.library")
    id("synco.serialization")
}

dependencies {
    api(project(":core:protocol"))
    api(project(":core:crypto"))
    implementation(libs.ktor.network)
}
