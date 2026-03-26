package tech.skot.tools.gradle

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import tech.skot.Versions
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

const val SKOT_ANDROID_PROPERIES_FILE_NAME = "skot_android.properties"
const val SKOT_IMPORTS_PROPERIES_FILE_NAME = "skot_imports.properties"

fun skReadAndroidProperties(path: Path): SKAndroidProperties? {
    val propertiesPath = path.resolve(SKOT_ANDROID_PROPERIES_FILE_NAME)
    return if (Files.exists(propertiesPath)) {
        val properties = Properties()
        properties.load(FileInputStream(propertiesPath.toFile()))
        SKAndroidProperties(properties)
    } else {
        null
    }
}

class SKAndroidProperties(private val properties: Properties) {
    val minSdk: Int?
        get() = (properties["minSdk"] as? String)?.toInt()

    val leakCanary: Boolean?
        get() = (properties["leakCanary"] as? String)?.toBoolean()
}

fun Project.skReadAndroidProperties(): SKAndroidProperties? = skReadAndroidProperties(rootProject.rootDir.toPath())

fun skReadImportsProperties(path: Path): SKImportsProperties? {
    val propertiesPath = path.resolve(SKOT_IMPORTS_PROPERIES_FILE_NAME)
    return if (Files.exists(propertiesPath)) {
        val properties = Properties()
        properties.load(FileInputStream(propertiesPath.toFile()))
        SKImportsProperties(properties)
    } else {
        null
    }
}

class SKImportsProperties(private val properties: Properties) {
    val ktor2: Boolean?
        get() {
            return (properties["ktor2"] as? String?)?.toBoolean()
        }
}

fun Project.skReadImportsProperties(): SKImportsProperties? = skReadImportsProperties(rootProject.rootDir.toPath())

// For pure Android library modules (com.android.library)
fun LibraryExtension.androidBaseConfig(project: Project) {
    val androidProperties = project.skReadAndroidProperties()
    defaultConfig {
        minSdk = androidProperties?.minSdk ?: Versions.android_minSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileSdk = Versions.android_compileSdk
    lint {
        abortOnError = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// For Android application modules (com.android.application)
fun ApplicationExtension.androidBaseConfig(project: Project) {
    val androidProperties = project.skReadAndroidProperties()
    defaultConfig {
        minSdk = androidProperties?.minSdk ?: Versions.android_minSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        targetSdk = Versions.android_compileSdk
    }
    compileSdk = Versions.android_compileSdk
    lint {
        abortOnError = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// For KMP library modules (com.android.kotlin.multiplatform.library)
fun KotlinMultiplatformAndroidLibraryExtension.androidBaseConfig(project: Project) {
    val androidProperties = project.skReadAndroidProperties()
    minSdk = androidProperties?.minSdk ?: Versions.android_minSdk
    compileSdk = Versions.android_compileSdk
}
