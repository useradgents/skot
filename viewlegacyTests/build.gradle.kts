group = Versions.group
version = Versions.version

plugins {
    id("com.android.library")
    id("maven-publish")
    signing
}

android {
    namespace = "tech.skot.viewlegacytests"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    publishing {
        singleVariant("release")
    }

    buildFeatures {
        viewBinding = true
    }

    sourceSets {
        getByName("main") {
            kotlin {
                directories.add("src/androidMain/kotlin")
            }
            res {
                directories.add("src/androidMain/res")
            }
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
    }
}

dependencies {
    implementation(project(":viewlegacy"))
    api(libs.espresso.core)
    api(libs.core.ktx)
    api(libs.junit.ktx)
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
