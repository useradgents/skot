plugins {
	kotlin("multiplatform")
	id("tech.skot.model")
}

kotlin {
	android {
		namespace = "io.uad.skotsample.model"
		compileSdk = 36
		minSdk = 23
	}
}
