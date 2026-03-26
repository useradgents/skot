group = "io.uad.skotsample"
version = Build.versionName

plugins {
	kotlin("multiplatform")
	id("tech.skot.modelcontract")
}

kotlin {
	android {
		namespace = "io.uad.skotsample.modelcontract"
		compileSdk = 36
		minSdk = 23
	}
}
