plugins {
	kotlin("multiplatform")
	id("tech.skot.model")
}

kotlin {
	android {
		namespace = "io.uad.skotsample.model"
		compileSdk = 37
		minSdk = 23
	}
}
