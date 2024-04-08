import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

group = Versions.group
version = Versions.version

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
    signing
}

dependencies {
    api(libs.core)
    api(libs.appcompat)
    api(libs.androidx.activity)
    api(libs.constraintlayout)
    api(libs.viewpager2)
    api(libs.recyclerview)
    api(libs.material)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.lifecycle.runtime.ktx)
    androidTestImplementation(project(":viewlegacyTests"))
}

android {
    lint {
        baseline = file("lint-baseline.xml")
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    namespace = "tech.skot.viewlegacy"
    testNamespace = "tech.skot.viewlegacytests"
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

    sourceSets["commonMain"].kotlin.srcDir("generated/commonMain/kotlin")
    sourceSets["commonMain"].dependencies {
        api(project(":core"))
        api(project(":viewcontract"))
    }

    sourceSets["androidMain"].dependencies {
    }

    // sourceSets["androidInstrumentedTest"].resources.srcDir("src/androidMain/res_test")

    println("-----@@@@@@@---- ${sourceSets.asMap}")
}
