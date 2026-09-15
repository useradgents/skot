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