group = Versions.group
version = Versions.version

plugins {
    id("com.android.library")
    id("maven-publish")
    signing
}

kotlin {
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_4)
        optIn.add("kotlin.time.ExperimentalTime")
    }
}

dependencies {
    api(project(":core"))
    api(project(":coreAndroid"))
    api(project(":viewcontract"))
    api(project(":modelcontract"))

    testImplementation(libs.jetbrains.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

android {
    namespace = "tech.skot.viewmodel"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    lint {
        abortOnError = false
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
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
