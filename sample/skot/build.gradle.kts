plugins {
	id("tech.skot.tools")
}

skot {
    app = tech.skot.tools.gradle.App(
            startScreen = ".screens.SplashVC",
            packageName = "io.uad.skotsample",
            baseActivity = ".android.BaseActivity")
}                