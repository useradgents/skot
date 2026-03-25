group = "io.uad.skotsample"
version = Build.versionName

plugins {
	kotlin("multiplatform")
	id("tech.skot.viewcontract")
}
android {
	namespace = "io.uad.skotsample.viewcontract"
}