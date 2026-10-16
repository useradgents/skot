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
    implementation(libs.jetbrains.kotlin.stdlib)
    api(project(":viewcontract"))
    implementation(project(":modelcontract"))
    api(libs.kotlinpoet)
    api(kotlin("reflect"))
    implementation(libs.jetbrains.kotlin.compiler.embeddable)

    testImplementation(libs.jetbrains.kotlin.test.junit)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
