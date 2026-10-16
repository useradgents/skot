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
}

java {
    withSourcesJar()
}

dependencies {
    api(project(":core"))
    api(libs.jetbrains.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.jetbrains.kotlin.test.junit)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
