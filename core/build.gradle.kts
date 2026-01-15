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
        optIn.add("kotlin.time.ExperimentalTime")
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    jvm()

    androidLibrary {
        namespace = "tech.skot.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.coroutines.core)
                api(kotlin("reflect"))
                api(libs.kotlinx.datetime)
            }
        }

        jvmMain {
            dependencies {
                api(libs.bundles.kotlinx.coroutines)
            }
        }

        androidMain {
            dependencies {
                api(libs.timber)
                api(libs.bundles.kotlinx.coroutines)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.jetbrains.kotlin.test.junit)
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.espresso.core)
                implementation(libs.core.ktx)
                implementation(libs.junit.ktx)
                implementation(libs.jetbrains.kotlin.test.junit)
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
    }
}