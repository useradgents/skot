# Setup Project environment

## Add Environment (Prod, Preprod, Rec)

Environments are the `environment` dimension of the Android product flavors (required). They are
declared in `skot_flavors.properties`, at the root of the project:

```properties
environment=rec,preprod,prod
```

* names are lowerCamelCase (`preprod`, not `Preprod`), the first value is the default one

* launch gradle sync: the variants `recDebug`, `preprodRelease`... appear in the Build Variants selector

* environment specific files go in `src/<environment>/` of each module (`src/prod/kotlin`,
  `src/prod/res_referenced`...); a file specific to a brand and an environment goes in
  `src/<brand><Environment>/` (`src/app1Prod/kotlin`)

* app settings of an environment go in `androidApp/build.gradle.kts`:

```kotlin
android.productFlavors {
    named("rec") { applicationIdSuffix = ".rec" }
}
```

* constants of an environment used by the contracts (API urls...) go in the `:variant` module,
  in `variant/src/<environment>/kotlin`

## Change current Env

There is no switch task: pick the variant in the Build Variants selector of Android Studio.

## Build on ci/cd

```bash
#!/usr/bin/env bash
./gradlew assembleProdRelease
```
