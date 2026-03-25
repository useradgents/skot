# Migration vers Skot 1.4.4b-ua (AGP 9.0)

Cette version migre le framework vers **Android Gradle Plugin 9.0** et **Gradle 9.x**.
Elle introduit des changements incompatibles avec les projets existants.

---

## Ce qui change

| Domaine | Avant | Après |
|---|---|---|
| AGP | 8.x | 9.0+ |
| Gradle wrapper | 8.x | 9.3.1+ |
| Plugin KMP library | `com.android.library` + KMP | `com.android.kotlin.multiplatform.library` |
| Sources Kotlin dans les plugins | `java.srcDir(...)` | `kotlin.srcDir(...)` |
| `org.jetbrains.kotlin.android` | présent (app/view) | supprimé (AGP 9.0 built-in Kotlin) |
| `androidTarget { publishLibraryVariants(...) }` | présent dans KMP modules | supprimé |

---

## 1. Gradle wrapper

Mettre à jour `gradle/wrapper/gradle-wrapper.properties` :

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.3.1-bin.zip
```

---

## 2. `gradle.properties`

Supprimer les propriétés supprimées dans AGP 9.0 si présentes :

```properties
# SUPPRIMER ces lignes si elles existent :
android.enableLegacyVariantApi=true
android.r8.integratedResourceShrinking=...
android.enableNewResourceShrinker.preciseShrinking=...
android.builtInKotlin=false
```

Conserver :
```properties
android.enableJetifier=true
android.useAndroidX=true
org.gradle.jvmargs=-XX\:MaxHeapSize\=4096 -Xmx4096M
org.gradle.daemon=true
```

---

## 3. `build.gradle.kts` racine

Mettre à jour la version du plugin Skot :

```kotlin
buildscript {
    dependencies {
        classpath("com.github.useradgents.skot:plugin:1.4.4b-ua")
    }
}
```

---

## 4. `settings.gradle.kts`

Ajouter `mavenLocal()` dans `pluginManagement.repositories` si absent (utile pour les builds locaux) :

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}
```

---

## 5. Modules KMP (`viewcontract`, `modelcontract`, `viewmodel`, `model`)

### 5a. Ajouter `compileSdk` et `minSdk` explicitement

Les plugins Skot ne peuvent plus injecter ces valeurs automatiquement dans les modules KMP avec AGP 9.0. Il faut les déclarer dans le `build.gradle.kts` de chaque module KMP :

**Avant :**
```kotlin
plugins {
    kotlin("multiplatform")
    id("tech.skot.viewmodel") // ou model / viewcontract / modelcontract
}

kotlin {
    android {
        namespace = "com.monapp.viewmodel"
    }
}
```

**Après :**
```kotlin
plugins {
    kotlin("multiplatform")
    id("tech.skot.viewmodel") // ou model / viewcontract / modelcontract
}

kotlin {
    android {
        namespace = "com.monapp.viewmodel"
        compileSdk = 36
        minSdk = 23
    }
}
```

> Appliquer ce changement pour : `viewcontract`, `modelcontract`, `viewmodel`, `model`

### 5b. Supprimer `androidTarget { publishLibraryVariants(...) }` si présent

```kotlin
// SUPPRIMER ce bloc s'il est présent dans un module KMP :
androidTarget {
    publishLibraryVariants("release")
}
```

---

## 6. Module `view` (tech.skot.viewlegacy)

Aucun changement nécessaire dans le `build.gradle.kts`. Le plugin gère tout.

```kotlin
plugins {
    id("tech.skot.viewlegacy")
}

android {
    namespace = "com.monapp.view"
}
```

---

## 7. Module `androidApp` (tech.skot.app)

Aucun changement nécessaire. Le plugin `tech.skot.app` applique automatiquement `com.android.application` sans `org.jetbrains.kotlin.android` (supprimé dans AGP 9.0).

Si le projet déclarait explicitement `kotlin("android")` ou `org.jetbrains.kotlin.android`, les supprimer dans **tous les modules** (`androidApp`, `view`, et dans le `build.gradle.kts` racine avec `apply false`) :

```kotlin
// SUPPRIMER ces formes partout :
kotlin("android")
kotlin("android") version "2.x.x" apply false
id("org.jetbrains.kotlin.android")
id("org.jetbrains.kotlin.android") version "2.x.x" apply false
```

Si le projet utilise un version catalog (`libs.versions.toml`), supprimer aussi l'entrée si elle n'est plus référencée ailleurs :
```toml
# SUPPRIMER si présent et inutilisé :
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

---

## Vérification post-migration

```bash
./gradlew :androidApp:assembleDebug
```

Le build doit être `BUILD SUCCESSFUL` sans `compileDebugKotlin NO-SOURCE`.

---

## Script de migration automatique

Voir `scripts/migrate-to-1.4.4b-ua.sh` pour une migration assistée.
