## Add variant for project

## Add a brand (APP1, APP2)

Brands are the `brand` dimension of the Android product flavors. They are declared in
`skot_flavors.properties`, at the root of the project (the `brand` line is optional):

```properties
brand=app1,app2
environment=dev,prod
```

* names are lowerCamelCase (`app1`, not `App1`), the first value of each dimension is the default one

* launch gradle sync: the variants `app1DevDebug`, `app2ProdRelease`... appear in the Build Variants selector

* brand specific files go in `src/<brand>/` of each module:
  * `src/app1/kotlin` for code (same file path in every brand)
  * `src/app1/res` for Android resources, `src/app1/res_referenced` for the referenced ones
    (strings, colors, fonts, icons): every brand must provide the same resource names, the generator
    stops and names a resource missing in a variant
  * `androidApp/src/app1/` for the `google-services.json` of the brand

* app settings of a brand (`applicationId`, `manifestPlaceholders`, signing) go in
  `androidApp/build.gradle.kts`:

```kotlin
android.productFlavors {
    named("app2") { applicationId = "com.app2.android" }
}
```

* constants of a brand used by the contracts go in the `:variant` module, in `variant/src/<brand>/kotlin`

## Change current variant

There is no switch task: pick the variant in the Build Variants selector of Android Studio.

## Build on ci/cd

```bash
#!/usr/bin/env bash
./gradlew skot:skGenerate
./gradlew assembleApp1ProdRelease
```
