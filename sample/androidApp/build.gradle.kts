plugins {
    kotlin("android")
	id("tech.skot.app")
}

android {

    defaultConfig {
        applicationId = "io.uad.skotsample"
        
        versionCode = Build.versionCode
        versionName = Build.versionName
    }
    namespace = "io.uad.skotsample.android"


    buildTypes {
        getByName("debug") {
            applicationIdSuffix = "dev"
            manifestPlaceholders["app_name"] = "D_${Build.appName}"
        }

        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

//            signingConfig = signingConfigs.getByName("release")

            manifestPlaceholders["app_name"] = Build.appName
        }
    }

}