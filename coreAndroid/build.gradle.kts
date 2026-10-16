group = Versions.group
version = Versions.version

plugins {
    id("com.android.library")
    id("maven-publish")
    signing
}

dependencies {
    api(project(":core"))
    api(libs.timber)
    api(libs.kotlinx.coroutines.android)

    testImplementation(libs.jetbrains.kotlin.test.junit)

    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.core.ktx)
    androidTestImplementation(libs.junit.ktx)
    androidTestImplementation(libs.jetbrains.kotlin.test.junit)
}

android {
    namespace = "tech.skot.core.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    // Les tests locaux construisent une Application de test : le android.jar mockable doit
    // renvoyer des valeurs par défaut au lieu de lever "Method ... not mocked".
    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    lint {
        abortOnError = false
    }

    publishing {
        singleVariant("release")
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
            }
        }
    }
}
