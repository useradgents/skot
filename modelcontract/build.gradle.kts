plugins {
    kotlin("jvm")
    `java-library`
    id("maven-publish")
    signing
}

group = Versions.group
version = Versions.version

kotlin {
    jvmToolchain(21)
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
    }
}

java {
    withSourcesJar()
}

dependencies {
    api(project(":core"))
    api(libs.jetbrains.kotlin.stdlib)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.core)
    api(libs.kotlinx.datetime)

    testImplementation(libs.jetbrains.kotlin.test.junit)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
