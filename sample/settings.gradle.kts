pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "SkotSample"

include(":viewcontract")
include(":modelcontract")
include(":view")
include(":model")
include(":viewmodel")
include(":androidApp")
include(":skot")