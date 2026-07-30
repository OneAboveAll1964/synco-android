plugins {
    id("synco.jvm.library")
    id("synco.serialization")
}

dependencies {
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.coroutines.core)
}
