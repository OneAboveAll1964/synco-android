plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

tasks.register("verifyProtocol") {
    val local = rootProject.file("PROTOCOL.md")
    val sibling = rootProject.file("../synco-macos/PROTOCOL.md")
    doLast {
        if (sibling.exists() && !local.readBytes().contentEquals(sibling.readBytes())) {
            throw GradleException("PROTOCOL.md differs between synco-android and synco-macos; align them first")
        }
    }
}
