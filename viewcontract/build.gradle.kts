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
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.jetbrains.kotlin.test.junit)
            }
        }
    }
}
