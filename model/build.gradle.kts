group = Versions.group
version = Versions.version

plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
    id("maven-publish")
    signing
}

kotlin {
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_4)
    }
}

dependencies {
    api(project(":core"))
    api(project(":coreAndroid"))
    api(project(":modelcontract"))
    api(libs.sqldelight.runtime)
    api(libs.sqldelight.android.driver)
    api(libs.kotlinx.serialization.json)
    api(libs.ktor.client.core)
    api(libs.ktor.ktor.client.auth)
    api(libs.ktor.ktor.client.logging)
    api(libs.ktor.ktor.client.okhttp)

    testImplementation(project(":modelTests"))

    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.core.ktx)
    androidTestImplementation(libs.junit.ktx)
    androidTestImplementation(libs.jetbrains.kotlin.test.junit)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}

android {
    namespace = "tech.skot.model"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    lint {
        abortOnError = false
    }

    publishing {
        singleVariant("release")
    }
}

sqldelight {
    databases {
        create("PersistDb") {
            packageName.set("tech.skot.model.persist")
        }
    }
    linkSqlite = false
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
