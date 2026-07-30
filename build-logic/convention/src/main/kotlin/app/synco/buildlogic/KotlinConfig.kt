package app.synco.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.configureAndroidKotlin(extension: CommonExtension<*, *, *, *, *, *>) {
    extension.compileSdk = BuildConstants.COMPILE_SDK
    extension.defaultConfig.minSdk = BuildConstants.MIN_SDK

    extension.compileOptions.sourceCompatibility = BuildConstants.JAVA_VERSION
    extension.compileOptions.targetCompatibility = BuildConstants.JAVA_VERSION

    extension.packaging.resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    extension.packaging.resources.excludes.add("/META-INF/versions/9/OSGI-INF/MANIFEST.MF")

    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        compilerOptions.freeCompilerArgs.addAll(COMPILER_ARGS)
    }
}

internal fun Project.configureJvmKotlin() {
    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(BuildConstants.JVM_TARGET))
    }

    extensions.configure<KotlinJvmProjectExtension> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        compilerOptions.freeCompilerArgs.addAll(COMPILER_ARGS)
    }
}

private val COMPILER_ARGS = listOf(
    "-opt-in=kotlin.ExperimentalStdlibApi",
    "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
    "-opt-in=kotlinx.coroutines.FlowPreview",
)
