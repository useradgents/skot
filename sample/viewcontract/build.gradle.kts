group = "io.uad.skotsample"
version = SKBuild.versionName

plugins {
	kotlin("multiplatform")
	id("tech.skot.viewcontract")
}

kotlin {
	android {
		namespace = "io.uad.skotsample.viewcontract"
		compileSdk = 36
		minSdk = 23
	}
}
