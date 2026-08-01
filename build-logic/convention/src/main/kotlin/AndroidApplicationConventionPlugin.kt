import app.synco.buildlogic.BuildConstants
import app.synco.buildlogic.configureAndroidKotlin
import app.synco.buildlogic.libs
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.android")

        extensions.configure<ApplicationExtension> {
            configureAndroidKotlin(this)
            defaultConfig {
                applicationId = BuildConstants.APPLICATION_ID
                targetSdk = BuildConstants.TARGET_SDK
                versionCode = BuildConstants.VERSION_CODE
                versionName = BuildConstants.VERSION_NAME
            }
            val keystoreProperties = java.util.Properties().apply {
                val file = target.rootProject.file("keystore.properties")
                if (file.exists()) file.inputStream().use { load(it) }
            }
            if (keystoreProperties.isNotEmpty()) {
                signingConfigs.create("release") {
                    storeFile = target.rootProject.file(keystoreProperties.getProperty("storeFile"))
                    storePassword = keystoreProperties.getProperty("storePassword")
                    keyAlias = keystoreProperties.getProperty("keyAlias")
                    keyPassword = keystoreProperties.getProperty("keyPassword")
                }
            }
            buildTypes {
                getByName("release") {
                    signingConfig = signingConfigs.findByName("release")
                    isMinifyEnabled = true
                    isShrinkResources = true
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro",
                    )
                }
                getByName("debug") {
                    applicationIdSuffix = ".debug"
                }
            }
        }

        dependencies {
            add("implementation", libs.findLibrary("kotlinx-coroutines-android").get())
            add("testImplementation", libs.findLibrary("junit").get())
        }
    }
}
