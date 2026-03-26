group = Versions.group
version = Versions.version

plugins {
    id("com.android.library")
    id("maven-publish")
    signing
}

dependencies {
    api(project(":core"))
    api(project(":viewcontract"))
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
    namespace = "tech.skot.viewlegacy"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    testNamespace = "tech.skot.viewlegacytests"

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = false
    }

    sourceSets {
        getByName("main") {
            java.srcDir("src/androidMain/kotlin")
            java.srcDir("generated/androidMain/kotlin")
            res.srcDir("src/androidMain/res")
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
        getByName("androidTest") {
            java.srcDir("generated/androidTest/kotlin")
        }
    }

    buildFeatures {
        viewBinding = true
    }
}
