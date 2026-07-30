plugins {
    id("synco.jvm.library")
}

dependencies {
    api(project(":core:protocol"))
    implementation(libs.bouncycastle.prov)
}
