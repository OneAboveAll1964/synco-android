package app.synco.buildlogic

import org.gradle.api.JavaVersion

object BuildConstants {
    const val COMPILE_SDK = 36
    const val MIN_SDK = 29
    const val TARGET_SDK = 36
    const val APPLICATION_ID = "com.shkomaghdid.synco.android"
    const val VERSION_CODE = 3
    const val VERSION_NAME = "1.0.2"
    val JAVA_VERSION: JavaVersion = JavaVersion.VERSION_17
    const val JVM_TARGET = 17
}
