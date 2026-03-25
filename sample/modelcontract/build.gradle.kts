group = "io.uad.skotsample"
version = Build.versionName

plugins {
	kotlin("multiplatform")
	id("tech.skot.modelcontract")
}

android {
	namespace = "io.uad.skotsample.modelcontract"
}
