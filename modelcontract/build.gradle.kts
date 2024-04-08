
plugins {
    kotlin("multiplatform")
    id("maven-publish")
    signing
}

group = Versions.group
version = Versions.version

kotlin {
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                api(project(":core"))
                api(libs.jetbrains.kotlin.stdlib)
                api(libs.kotlinx.coroutines.core)
                api(libs.jetbrains.kotlinx.serialization.core)
                api(libs.kotlinx.datetime)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.jetbrains.kotlin.test.junit)
            }
        }
    }
}
