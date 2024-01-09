import java.util.Locale

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("kotlinx-serialization")
    id("com.squareup.sqldelight")
    id("maven-publish")
    signing
}

group = Versions.group
version = Versions.version



kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm()

    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
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


        val androidInstrumentedTest by getting {
            dependencies {
                implementation(libs.espresso.core)
                implementation(libs.core.ktx)
                implementation(libs.junit.ktx)
                implementation(libs.jetbrains.kotlin.test.junit)
            }
        }

        val androidUnitTest by getting {
            dependencies {
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
    namespace = "tech.skot.model"



    packaging {
        if (gradle.startParameter.taskNames.any {
                it.uppercase(Locale.getDefault()).contains("ANDROIDTEST")
            }) {
            resources.excludes.add("META-INF/*")
        }
    }


}

dependencies {

    androidTestImplementation(libs.jetbrains.kotlin.test.junit)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.core.ktx)
    androidTestImplementation(libs.junit.ktx)
}

sqldelight {

    this.database("PersistDb") {
        packageName = "tech.skot.model.persist"
    }
    linkSqlite = false
}
