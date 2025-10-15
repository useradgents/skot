import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi


plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
    signing
}

group = Versions.group
version = Versions.version

kotlin {
    jvmToolchain(17)
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
    }

    jvm()

    androidTarget {
        publishLibraryVariants("release")
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":core"))
                api(project(":viewcontract"))
                api(project(":modelcontract"))
            }
        }
    }
}

dependencies {
    testImplementation(libs.jetbrains.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

android {
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    namespace = "tech.skot.viewmodel"

    sourceSets {
        getByName("main").manifest.srcFile("src/androidMain/AndroidManifest.xml")
        getByName("test").java.srcDirs("src/javaTest/kotlin")
    }
}
