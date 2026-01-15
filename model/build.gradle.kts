import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
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
        namespace = "tech.skot.model"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withHostTest {}

        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":core"))
                api(project(":modelcontract"))
                api(libs.sqldelight.runtime)
                api(libs.kotlinx.serialization.json)
                api(libs.ktor.client.core)
                api(libs.ktor.ktor.client.auth)
                api(libs.ktor.ktor.client.logging)
            }
        }

        androidMain {
            dependencies {
                api(libs.sqldelight.android.driver)
                api(libs.ktor.ktor.client.okhttp)
            }
        }

        jvmMain {
            dependencies {
                api(libs.sqldelight.sqlite.driver)
                api(libs.ktor.ktor.client.okhttp)
                api(libs.ktor.ktor.client.mock.jvm)
                api(libs.jetbrains.kotlin.test.junit)
                api(libs.kotlinx.coroutines.test)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.jetbrains.kotlin.test.junit)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.jetbrains.kotlin.test.junit)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.espresso.core)
                implementation(libs.core.ktx)
                implementation(libs.junit.ktx)
            }
        }

        getByName("androidHostTest") {
            dependencies {
                implementation(libs.jetbrains.kotlin.test.junit)
            }
        }
    }
}

sqldelight {
    this.database("PersistDb") {
        packageName = "tech.skot.model.persist"
    }
    linkSqlite = false
}