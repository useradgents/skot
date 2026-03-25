import tech.skot.tools.gradle.skVersionCode

plugins {
	id("tech.skot.app")
}

android {

    defaultConfig {
        applicationId = "io.uad.skotsample"

        versionCode = skVersionCode()
        versionName = SKBuild.versionName
    }
    namespace = "io.uad.skotsample.android"


    buildTypes {
        getByName("debug") {
            applicationIdSuffix = "dev"
            manifestPlaceholders["app_name"] = "D_${SKBuild.appName}"
        }

        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

//            signingConfig = signingConfigs.getByName("release")

            manifestPlaceholders["app_name"] = SKBuild.appName
        }
    }

}
