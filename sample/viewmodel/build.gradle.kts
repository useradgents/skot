plugins {
	kotlin("multiplatform")
	id("tech.skot.viewmodel")
}

kotlin {
	android {
		namespace = "io.uad.skotsample.viewmodel"
		compileSdk = 36
		minSdk = 23
	}
}
