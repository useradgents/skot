import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

group = Versions.group
version = Versions.version

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
    signing
}

kotlin {
    jvmToolchain(17)
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
    }
    androidTarget("android") {
        publishLibraryVariants("release", "debug")
        publishLibraryVariantsGroupedByFlavor = true
    }
}

android {
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    namespace = "tech.skot.viewlegacytests"
}

dependencies {
    implementation((project(":viewlegacy")))
    api(libs.espresso.core)
    api(libs.core.ktx)
    api(libs.junit.ktx)
}
