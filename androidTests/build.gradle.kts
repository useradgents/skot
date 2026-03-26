group = Versions.group
version = Versions.version

plugins {
    id("com.android.library")
    id("maven-publish")
}

android {
    namespace = "tech.skot.androidtests"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    sourceSets {
        getByName("main") {
            java.srcDir("src/androidMain/kotlin")
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
    }
}

dependencies {
    api(libs.jetbrains.kotlin.stdlib)
    api(libs.kotlinx.coroutines.android)
    api(libs.appcompat)
    api(libs.kotlin.test)
    api(libs.espresso.core)
    api(libs.espresso.contrib)
    api(libs.espresso.web)
    api(libs.test.rules)
    api(libs.core.testing)
    api(libs.ktor.server.netty)
    api(libs.ktor.client.android)
    api(libs.test.runner)
    implementation(project(":core"))
}
