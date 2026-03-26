group = Versions.group
version = Versions.version

plugins {
    id("com.android.library")
    id("maven-publish")
    signing
}

android {
    namespace = "tech.skot.viewlegacytests"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":viewlegacy"))
    api(libs.espresso.core)
    api(libs.core.ktx)
    api(libs.junit.ktx)
}
