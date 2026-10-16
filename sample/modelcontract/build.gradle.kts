group = "io.uad.skotsample"
version = SKBuild.versionName

plugins {
	id("tech.skot.modelcontract")
}

skot {
	buildFiles = listOf(SKBuild)
}
