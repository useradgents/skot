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
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_4)
        optIn.add("kotlin.time.ExperimentalTime")
    }
}

java {
    withSourcesJar()
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(kotlin("reflect"))
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
