import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
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

    androidLibrary {
        namespace = "tech.skot.viewmodel"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":core"))
                api(project(":viewcontract"))
                api(project(":modelcontract"))
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.jetbrains.kotlin.test.junit)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}