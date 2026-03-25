plugins {
	kotlin("multiplatform")
	id("tech.skot.model")
}

android {
	namespace = "io.uad.skotsample.model"
	lint {
		disable += "UnsafeOptInUsageError"
	}
}