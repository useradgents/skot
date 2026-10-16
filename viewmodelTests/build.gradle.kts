group = Versions.group
version = Versions.version

plugins {
    id("com.android.library")
    id("maven-publish")
    signing
}

dependencies {
    api(libs.jetbrains.kotlin.stdlib)
    api(libs.jetbrains.kotlin.test.junit)
    api(libs.kotlinx.coroutines.test)
    api(project(":core"))
    implementation(project(":viewmodel"))
    implementation(project(":viewcontract"))
    implementation(project(":modelcontract"))
}

android {
    namespace = "tech.skot.viewmodel.tests"
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
