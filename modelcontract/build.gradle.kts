
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

    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":core"))
                api(libs.jetbrains.kotlin.stdlib)
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.serialization.core)
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
