# Changelog

## Version `1.6.0-ua`

### chore

- #### Toolchain
    - Upgrade to **Kotlin 2.4.20**, **Android Gradle Plugin 9.4.0**, **Gradle 9.7.1** and
      **compileSdk / targetSdk 37**. AGP 9.4.0 requires Gradle 9.6.0 or later, hence the wrapper
      bump.
    - `apiVersion` moved from `KOTLIN_2_3` to `KOTLIN_2_4`, following the compiler — both in the
      framework's own modules and in the `tech.skot.*` plugins, which set it on the modules of the
      applications they configure.
- #### Dependencies
    - Kotlinx Coroutines 1.10.2 → 1.11.0, Kotlinx Serialization 1.10.0 → 1.11.0,
      Ktor 3.4.2 → 3.5.2, Kotlinx DateTime 0.7.1 → 0.8.0 (`-0.6.x-compat` variant kept),
      KotlinPoet 2.3.0 → 2.4.0.
    - AndroidX Core 1.18.0 → 1.19.0, AppCompat 1.7.1 → 1.8.0, Material 1.13.0 → 1.14.0,
      ConstraintLayout 2.2.1 → 2.2.2, Lifecycle 2.10.0 → 2.11.0.
    - Build plugins: SonarQube 7.5.0.8588, ben-manes versions 0.63.0,
      version-catalog-update 1.1.1.
    - Held back on purpose, excluded by the project's own `rejectVersionIf` filter: SQLDelight
      2.4.0-rc, LeakCanary 3.0-alpha, Kotlinx Serialization 1.12.0-RC.

- #### Deprecations
    - `plugin`: `AndroidSourceDirectorySet.srcDir()` / `srcDirs()` replaced by the `directories`
      mutable set (30 call sites in `PluginApp`, `PluginFeature`, `PluginLibraryViewLegacy` and
      `PluginViewLegacy`).
    - `PluginFeature`: drop the deprecated legacy `com.android.build.gradle.AppExtension` DSL. Its
      `compileSdk` / `minSdk` configuration moved into the `com.android.build.api.dsl.DynamicFeatureExtension`
      block the plugin already configures. **`targetSdk` is no longer set on feature modules**: the
      new DSL does not expose it on `DynamicFeatureBaseFlavor`, since a dynamic feature inherits it
      from the base application module.
    - `generator`, `viewmodelTests`: `val jvmMain by getting { }` replaced by the `jvmMain { }`
      accessor — the delegate syntax is scheduled for removal in Gradle 10.
    - Drop `kotlin.mpp.androidSourceSetLayoutVersion` from `gradle.properties`: layout V2 is the
      default and the property is no longer supported.
    - Apply the ben-manes versions plugin under its new `io.github.ben-manes.versions` id.

    The build no longer reports any deprecation coming from this repository. The remaining
    `-Xuse-fir-lt` warning is emitted by Gradle's own `kotlin-dsl` plugin on `:plugin:compileKotlin`.

### fix

- #### Gradle plugin
    - `tech.skot.modelcontract` is configuration cache compatible again, and stops generating a
      non-deterministic build file. `skCopyBuildFileDebug` and `skCopyBuildFileRelease` are replaced
      by a single `skCopyBuildFile` task.

      Both tasks wrote the *same* file — `generated/commonMain/kotlin`, shared by every variant —
      and an `onlyIf` block inspecting the task graph was meant to let only one of them run. That
      block captured `Project`, which the configuration cache cannot serialize, and it did not even
      work: on `./gradlew build` both `preDebugBuild` and `preReleaseBuild` are in the graph, so
      both tasks ran and the last one won. Debug code could be compiled against
      `SKBuild.debug = false`, depending on execution order.

      A compile-time constant living in a variant-shared source set cannot carry a per-variant
      value at all, whatever the task does: one Gradle invocation, one file, one value — and
      `./gradlew build` asks for both variants at once. **`SKBuild.debug` is therefore no longer a
      `const val`**, it is read at runtime:

          public val debug: Boolean
              get() = SKEnv.debug

      `SKEnv.debug` is set by `SKEnvInitProvider`, a `ContentProvider` declared in `core`'s
      manifest and merged into every application. Android creates content providers *before*
      `Application.onCreate()`, so the value is already correct for the whole of an application's
      initialization — including the code that runs before the injector is built. It is read from
      `ApplicationInfo.FLAG_DEBUGGABLE` rather than `BuildConfig.DEBUG`, `buildConfig` being off by
      default since AGP 8. On the JVM nothing sets it and it stays `false`, which is what the
      previous task-name heuristic already yielded for `jvmTest`.

      `skCopyBuildFile` no longer depends on the requested tasks, so the file it writes is
      identical in every invocation — deterministic, up-to-date-checkable, and free of the
      per-variant recompilation the previous scheme caused.

      Applications referencing `skCopyBuildFileDebug` or `skCopyBuildFileRelease` by name must use
      `skCopyBuildFile` instead.
- #### Code generation
    - `IconsMock` generation now emits `IconMock` through KotlinPoet's `%T` placeholder instead of a
      raw string, so the `tech.skot.core.view.IconMock` import is actually added. The generated file
      did not compile (`Unresolved reference 'IconMock'`) in any project declaring no icon at all —
      with at least one icon the import came in through the property types and hid the bug.
- #### Gradle plugin
    - `tech.skot.model` and `tech.skot.viewmodel` now set `failOnNoDiscoveredTests` to `false`. Both
      plugins register `generated/jvmTest` as a test source set and generate mocks and abstract base
      classes into it, so a module whose application has not written a test yet has test sources but
      no `@Test`. Gradle 9 treats that as a misconfiguration and fails `./gradlew build`.
    - Annotate `SkGenerateTask` and `SKCopyBuildFileTask` with `@DisableCachingByDefault`.
      `./gradlew build` was already failing on `:plugin:validatePlugins` before this release — a
      task type must declare either `@CacheableTask` or `@DisableCachingByDefault`. It went
      unnoticed because `scripts/check.sh` never builds the `plugin` module.

### Migrating an application to `1.6.0-ua`

Ordered steps. Each one is a hard prerequisite of the next: skipping one makes the following step
fail with an error that does not point back at it.

1. **Gradle wrapper to 9.6.0 at least** (9.7.1 recommended). AGP 9.4.0 simply refuses to apply on
   anything older:

       Minimum supported Gradle version is 9.6.0. Current version is 9.4.1.

   Note the chicken-and-egg: once the version catalog is bumped, `./gradlew wrapper` can no longer
   configure the build. Bump the wrapper *first*, or edit
   `gradle/wrapper/gradle-wrapper.properties` by hand (`distributionUrl` **and**
   `distributionSha256Sum`).

2. **Align AGP in the application's own version catalog** with the one carried by the plugin —
   `9.4.0`. `plugin/` exposes AGP as an `api` dependency, and Gradle refuses two AGP versions in
   one build:

       Using multiple versions of the Android Gradle Plugin [9.4.0, 9.1.0] across Gradle builds
       is not allowed.

3. **Bump Kotlin to `2.4.20`** and skot to `1.6.0-ua` in the same catalog.

   Check the third-party KMP tooling first, it is the step most likely to block. Validated on a
   production application: SKIE stops the build dead at configuration time, and takes the Android
   build down with it even though it only serves the Swift interop:

       Error: SKIE 0.10.11 does not support Kotlin 2.4.20.

   No published SKIE release supports 2.4.20 at the time of writing — 0.10.11 stops at Kotlin
   2.3.20, 0.10.14 (the latest) at 2.4.10. Until Touchlab ships support, the only way through is
   `skie { isEnabled = false }`, which disables the Swift interop of the shared module and
   therefore affects the iOS application. Compiler plugins tied to a Kotlin version — KSP, Compose,
   SKIE, Parcelize — all deserve the same check before bumping.

4. **Remove `kotlin.mpp.androidSourceSetLayoutVersion`** from `gradle.properties`. Layout V2 is the
   default and the property is no longer supported.

5. **Migrate the test sources off `runBlockingTest`** and the other deprecated
   `kotlinx-coroutines-test` APIs, removed in coroutines 1.11. `runTest` replaces them.

6. **Rename any reference to `skCopyBuildFileDebug` / `skCopyBuildFileRelease`** into
   `skCopyBuildFile`. Only relevant if a build script or a CI script names those tasks explicitly.

7. **Run `skGenerate`** and commit the result in its own commit. The diff is large but almost
   entirely cosmetic: indentation goes from 2 to 4 spaces and KotlinPoet 2.4.0 stops emitting
   redundant `kotlin.*` / `java.lang.*` imports. Keeping it separate leaves the real changes
   readable.

8. **Move every hand-pinned `compileSdk` to 37**, including the ones in git submodules. Modules
   configured by the `tech.skot.*` plugins follow on their own, reading the value from the
   generated `tech.skot.Versions` — but any module setting it itself stays behind, and the build
   then fails on an error naming the *library*, not the module that has to change:

       Dependency ':view' requires libraries and applications that depend on it to compile
       against version 37 or later of the Android APIs.
       :androidApp is currently compiled against android-36.

9. Java 21 is required, as it already was in `1.5.2-ua`.

10. Optionally, `org.gradle.configuration-cache=true`: the incompatibility in
    `tech.skot.modelcontract` is fixed in this release, so SKot no longer stands in the way. The
    application's own build scripts may still do, though — a script starting an external process at
    configuration time (`Runtime.getRuntime().exec`, a `git` call to install hooks) is reported the
    same way and has to be moved to execution time.

These steps were validated end to end on a production application: `assembleDebug` and
`assembleRelease` both succeed, and no configuration cache problem is reported by SKot itself.

### ⚠️ Breaking changes for applications

`plugin/` exposes AGP and the Kotlin Gradle plugin as `api` dependencies, so every application
applying a `tech.skot.*` plugin inherits this toolchain:

1. **Gradle 9.6.0 or later is now mandatory** — an older wrapper fails with
   `Minimum supported Gradle version is 9.6.0`.
2. **Kotlin 2.4 is now the minimum**, as a consequence of the `apiVersion` bump.
3. **Coroutines 1.11 removed `runBlockingTest`** and the other deprecated `kotlinx-coroutines-test`
   APIs of that era — application test suites still using them no longer compile. Migrate to
   `runTest`.
4. **The first `skGenerate` after upgrading rewrites every generated file**: indentation goes from
   2 to 4 spaces, and KotlinPoet 2.4.0 stops emitting redundant `kotlin.*` / `java.lang.*` imports.
   Expect a large diff carrying almost no semantic change.
5. **`SKBuild.debug` is no longer a compile-time constant.** Every ordinary read — `if
   (SKBuild.debug)`, `!SKBuild.debug`, passing it as an argument — keeps working unchanged. Only a
   use requiring a constant breaks: an annotation argument, a `when` branch on a constant, or
   another `const val` deriving from it. None was found in the applications checked.

   Its value is now correct in every build, which is a behaviour change in itself: an application
   whose CI runs `./gradlew build` or `assembleDebug assembleRelease` used to get
   `SKBuild.debug = false` in its debug artifact. Code gated on it — verbose network logging,
   internal-account checks, forced-update prompts — was silently taking the release branch there
   and now takes the debug one.

## Version `1.5.5-ua`

### fix

- #### Code generation
    - Generate a `PermissionsMock` that implements **every** abstract member of the project's
      `Permissions` interface. Only properties typed exactly `SKPermission` were emitted:
      `SKPermission?`, `List<SKPermission>` and members inherited from a parent interface were
      silently dropped, and the generated mock did not compile
      (`Class 'PermissionsMock' is not abstract and does not implement abstract members`).
      A nullable permission now gets a `SKPermissionMock` instance (a test needing `null` can
      subclass it), a `List<SKPermission>` is initialized with all the single permissions of the
      interface, and members carrying a default implementation (`get() = ...`) are left alone.
    - The generation now fails with an explicit message — naming the file, the declaration and the
      unsupported shape — instead of silently emitting code that doesn't compile, when a member of
      the `Permissions` interface has an unsupported shape (`Set<…>`, `Map<…>`, a non-`SKPermission`
      type, an abstract function…).

## Version `1.5.4-ua`

### fix

- #### Code generation
    - Do not propagate the annotations of a type's *declaration* onto the *usages* of that type in
      generated code. Since kotlin-stdlib 2.3.0, `kotlin.Pair` carries the compiler-internal
      `@kotlin.js.JsImplicitExport`, which KotlinPoet copied onto every parameter typed with a
      generic class. The generated model mocks no longer compiled
      (`Unresolved reference 'JsImplicitExport'`), breaking the whole `jvmTest` source set.
      Also affected the generated view proxies/mocks of `SKListVC` and `SKInputVC`.

## Version `1.5.3-ua`

### fix
- set referenceColorsByVariant and referenceFontsByVariant default value on PluginTools

### chore
- ignore files who start with dot

## Version `1.5.2-ua`

### chore
- Upgrade to Java 21

## Version `1.5.1-ua`

### feat

#### - Fonts Generation
  - Add `referenceFontsByVariant` option to generate fonts from variant-specific resource directories

#### - Colors Generation
  - Add `referenceColorsByVariant` option to generate colors from variant-specific resource directories

### fix

#### - Colors Generation
  - Fix wrong variable used for variant check (`referenceIconsByVariant` → `referenceColorsByVariant`)


## Version `1.5.0-ua`

### chore
- update AGP to 9
- update kotlin to 2.3.20

## Version `1.4.5-ua`

### feat

- #### Fonts Generation
    - Add `referenceFontsByVariant` option to generate fonts from variant-specific resource directories

- #### Colors Generation
    - Add `referenceColorsByVariant` option to generate colors from variant-specific resource directories

### fix

- #### Colors Generation
    - Fix wrong variable used for variant check (`referenceIconsByVariant` → `referenceColorsByVariant`)


## Version `1.4.4-ua`

### fix

- #### SkWebView
  - fix domStorageEnabled settings

## Version `1.4.3-ua`

### feat

- #### SKDateFormat
  - Add `timeZone` argument to `SKDateFormat` to specify the time zone used for formatting

## Version `1.4.2-ua`

### feat

- #### Colors Generation
    - Add support for variants in referenced colors generation
  
## Version  `1.4.1-ua`

### feat

- #### SkCombo
    - Make SKCombo choice item layout customizable

## Version `1.4.0-ua`

### CHORE

- remove ios target
- update libraries
- update to target sdk 36
- clean a lot of code

> [!IMPORTANT]  
> remove the line iOS = true or false if present in the skot block of skot/build.gradle.kts, in the app initialization


## Version `1.3.13-ua`

### feat

- #### SkInput
    - add fun getCursorSelection(onResult : (Pair<Int, Int>) -> Unit)

## Version `1.3.12-ua`

### chore

- update dependencies
- refactor code (replace deprecated code and remove some warning one generated code )

## Version `1.3.11-ua`

### fix

- #### Webview
    - save webview backstack on view destroy and restore it on go back to screen

## Version `1.3.10-ua`

### chore

- Update dependencies
- Remove some warning in generated code

## Version `1.3.9-ua`

### chore

- Update dependencies

## Version `1.3.8-ua`

### fix

- Center Snackbar horizontally

## Version `1.3.7-ua`

### chore

- update libraries + kotlin

## Version `1.3.6-ua`

### fix

- disable infinite scroll if only one image in gallery

## Version `1.3.5-ua`

### feat

- add infinite scroll on recyclerview

## Version `1.3.4-ua`

### chore

- update libraries + kotlin

## Version `1.3.3-ua`

### feat

- add exit() function on SKScreen to kill the Application

## Version `1.3.2-ua`

### feat

- add permissionRequestedConfig to SkWebView

## Version `1.3.1-ua`

### chore

- update to android API 35

## Version `1.3.0-ua`

### chore

- update to kotlin 2

## Version `1.2.43-ua`

- add onFocus and onFocusLost to SKInputWithSuggestions

## Version `1.2.42-ua`

### fix

- fix infinite scroll crash in list with 1 item

## Version `1.2.39-ua`

### FIX

- add the possibility to do something via the config webview content crash

## Version `1.2.38-ua`

### chore

- update libraries

## Version `1.2.37-ua`

### feat

- add generics annotation for generic ViewModel

## Version `1.2.36-ua`

### chore

- update kotlin
- update gradle
- update libraries

## Version `1.2.35-ua`

### fix

- #### KTLint
    - ignore Ktlint exit value during SkGenerate

## Version `1.2.34-ua`

### fix

- #### SkWebView
    - webview crash on some device

## Version `1.2.33-ua` (`fix 1.2.31-ua`)

### fix

- publish variant icons

## Version `1.2.32-ua`

### feat

- #### SKInput
    - add SKInputVC.Type.VisiblePassword for SkInput

## Version `1.2.31-ua`

### feat

- add drawable-nodpi in Icons file generation

## Version `1.2.30-ua`

### fix

- remove SKBottomSheet default transparent background

## Version `1.2.29-ua`

### chore

- update gradle
- replace deprecated OnBackPressed by onBackPressedDispatcher

## Version `1.2.28-ua`

### fix :

- fix test

### chore

- clean code

## Version `1.2.27-ua`

### fix :

- #### Deeplink
    - deeplink redirection after use of skot kill function

## Version `1.2.25-ua`

### fix :

- #### Webview
    - file chooser for webview, we have crash if webview is added on activity after activity
      creation.

## Version `1.2.24-ua`

Do not use, crash on this version

### fix :

- #### Webview
    - file chooser for webview fix
    -

## Version `1.2.23-ua`

Do not use, crash on this version

### feat :

- #### Webview
    - file chooser for webview

## Version `1.2.22-ua`

### chore :

- update gradle to version 8.4, AGP 8.1.2
- update kotlin to version 1.9.10
- use new versions catalog
- update targetSdk and compileSdk to 34

## Version `1.2.21-ua`

### chore :

- clean code

### feat

- #### SkSnackbar
    - Adding the isGestureInsetBottomIgnored parameter to remove the GestureInsetBottom and allow
      the snackbar to be displayed at the "true" bottom.

## Version `1.2.20-ua`

### fix :

- #### SkPager
    - pager update page on each index