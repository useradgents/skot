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
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm()
    androidTarget {
        publishLibraryVariants("release")
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
        val androidInstrumentedTest by getting {
            dependencies {
                implementation(libs.espresso.core)
                implementation(libs.core.ktx)
                implementation(libs.junit.ktx)
                implementation(libs.jetbrains.kotlin.test.junit)
            }
        }
    }
}

android {
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    namespace = "tech.skot.core"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
    }
}
