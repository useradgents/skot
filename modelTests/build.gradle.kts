group = Versions.group
version = Versions.version

plugins {
    id("com.android.library")
    id("maven-publish")
    signing
}

dependencies {
    api(project(":model"))
    api(libs.sqldelight.sqlite.driver)
    api(libs.ktor.ktor.client.mock.jvm)
    api(libs.jetbrains.kotlin.test.junit)
    api(libs.kotlinx.coroutines.test)
}

android {
    namespace = "tech.skot.model.tests"
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
