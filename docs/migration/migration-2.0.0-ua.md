# Migration vers SKot 2.0.0-ua (fin de Kotlin Multiplatform)

## Pourquoi

SKot 2 abandonne Kotlin Multiplatform : `core`, `viewcontract`, `modelcontract` et `generator` sont des
modules JVM, les autres des bibliothèques Android. Le code de votre projet ne bouge pas de dossier :
seuls les scripts Gradle des quatre modules et les déclarations `expect`/`actual` changent.

## Avant de commencer

1. Lancez l'inventaire (lecture seule, il n'écrit rien) et gardez sa sortie : elle sert de liste de
   travail pour les étapes 3 à 5.

   ```bash
   scripts/migration/2.0.0-ua/detect.sh <racine du projet>
   ```

   Il est livré avec ce dépôt : copiez-le ou appelez-le par son chemin absolu.
2. Mesurez le nombre de tests actuels (à retrouver à l'identique à l'étape 6) :

   ```bash
   ./gradlew viewmodel:jvmTest model:jvmTest --continue
   find . -path '*/build/test-results/jvmTest/TEST-*.xml' | xargs grep -ho 'tests="[0-9]*"' | awk -F'"' '{s+=$2} END {print "tests de référence :", s}'
   ```

3. Faites un instantané des fichiers générés :

   ```bash
   tar czf /tmp/generated-before.tgz $(find . -type d -name generated -not -path '*/build/*')
   ```

4. Facultatif : les répertoires `~/.m2/repository/com/github/useradgents/skot/*-jvm` et `*-android`
   éventuellement présents (anciens artefacts KMP) ne sont plus utilisés, vous pouvez les supprimer.

## Étape 1 - Version

Dans `gradle/libs.versions.toml` :

```toml
skot = "2.0.0-ua"
```

Si le `build.gradle.kts` racine déclare le plugin SKot en dur dans le classpath du `buildscript`,
changez aussi sa version :

```kotlin
classpath("com.github.useradgents.skot:plugin:2.0.0-ua")   // était 1.6.0-ua
```

## Étape 2 - Scripts des modules

`kotlin("multiplatform")` et le bloc `kotlin { android { … } sourceSets { … } }` disparaissent.
`compileSdk` et `minSdk` ne sont plus déclarés (le plugin les fournit, via `skot_android.properties` ou
les valeurs par défaut du framework). `namespace` reste dans `android { }` uniquement pour `viewmodel`
et `model`.

`viewcontract/build.gradle.kts`

```kotlin
// Avant
group = "io.uad.skotsample"
version = SKBuild.versionName

plugins {
	kotlin("multiplatform")
	id("tech.skot.viewcontract")
}

kotlin {
	android {
		namespace = "io.uad.skotsample.viewcontract"
		compileSdk = 37
		minSdk = 23
	}
}

// Après
group = "io.uad.skotsample"
version = SKBuild.versionName

plugins {
	id("tech.skot.viewcontract")
}
```

`modelcontract/build.gradle.kts`

```kotlin
// Avant
group = "io.uad.skotsample"
version = SKBuild.versionName

plugins {
	kotlin("multiplatform")
	id("tech.skot.modelcontract")
}

kotlin {
	android {
		namespace = "io.uad.skotsample.modelcontract"
		compileSdk = 37
		minSdk = 23
	}
}

skot {
	buildFiles = listOf(SKBuild)
}

// Après
group = "io.uad.skotsample"
version = SKBuild.versionName

plugins {
	id("tech.skot.modelcontract")
}

skot {
	buildFiles = listOf(SKBuild)
}
```

`viewmodel/build.gradle.kts`

```kotlin
// Avant
plugins {
	kotlin("multiplatform")
	id("tech.skot.viewmodel")
}

kotlin {
	android {
		namespace = "io.uad.skotsample.viewmodel"
		compileSdk = 37
		minSdk = 23
	}
}

// Après
plugins {
	id("tech.skot.viewmodel")
}

android {
	namespace = "io.uad.skotsample.viewmodel"
}
```

`model/build.gradle.kts`

```kotlin
// Avant
plugins {
	kotlin("multiplatform")
	id("tech.skot.model")
}

kotlin {
	android {
		namespace = "io.uad.skotsample.model"
		compileSdk = 37
		minSdk = 23
	}
}

// Après
plugins {
	id("tech.skot.model")
}

android {
	namespace = "io.uad.skotsample.model"
}
```

Règles pour les dépendances propres au projet :

- `androidMain.dependencies { … }` et `commonMain.dependencies { … }` (dans `kotlin { sourceSets { … } }`)
  deviennent un bloc `dependencies { … }` de premier niveau, avec `implementation` ou `api` selon ce que
  vous exposiez.
- Les dépendances Android d'un contrat (par exemple `play-services-maps` dans le `viewcontract` d'Argos)
  sont supprimées : un module JVM ne peut pas les utiliser, et `commonMain` ne les voyait déjà pas.
- Les dépendances `testImplementation` de `viewmodelTests` / `modelTests` sont ajoutées par le plugin.

Un `package="…"` resté dans le `AndroidManifest.xml` de `model` ou `viewmodel` est ignoré par AGP
(avertissement) : retirez-le, le namespace vit dans `android { namespace }`.

## Étape 3 - Code Android des contrats

Un contrat est un module JVM pur : le plugin refuse un `src/androidMain/kotlin*` ou
`src/androidMain/res*` non vide. Déplacez chaque fichier listé par la section 2 de `detect.sh` vers le
même chemin relatif sous `model/src/androidMain/`, avec `git mv` pour garder l'historique :

```bash
mkdir -p model/src/androidMain/kotlin/<package>/utils
git mv modelcontract/src/androidMain/kotlin/<package>/utils/File.kt model/src/androidMain/kotlin/<package>/utils/File.kt
git mv modelcontract/src/androidMain/res/values/strings.xml model/src/androidMain/res/values/strings.xml
```

- Si `model/src/androidMain/res/values/strings.xml` existe déjà, fusionnez le contenu à la main puis
  `git rm` l'ancien fichier.
- Les références `<namespace>.modelcontract.R` deviennent `<namespace>.model.R`.
- Supprimez les `AndroidManifest.xml` des contrats : `git rm viewcontract/src/androidMain/AndroidManifest.xml modelcontract/src/androidMain/AndroidManifest.xml`.

## Étape 4 - expect/actual

Chaque `expect` devient une interface en code commun, son ancien `actual` Android devient
l'implémentation, son ancien `actual` `jvmMain` devient un faux de test, et les deux sont liés par
l'injecteur SKot. La déclaration `expect` d'origine **reste**, avec le même nom et la même signature,
comme simple délégué vers `get<Interface>()` : les sites d'appel ne changent pas. Un commit par
déclaration.

Exemple (`getToken` d'Argos) :

```kotlin
// model/src/commonMain/.../model/FCMToken.kt
interface FcmTokenProvider {
    fun getToken(onToken: (String) -> Unit)
}

fun getToken(onToken: (String) -> Unit) = get<FcmTokenProvider>().getToken(onToken)

// model/src/androidMain/.../model/FCMToken.kt  (ancien actual)
class FirebaseFcmTokenProvider : FcmTokenProvider {
    override fun getToken(onToken: (String) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { onToken(it) }
    }
}
// + single<FcmTokenProvider> { FirebaseFcmTokenProvider() } dans le module model de l'app

// model/src/jvmTest/.../FakeFcmTokenProvider.kt  (ancien actual jvmMain)
class FakeFcmTokenProvider : FcmTokenProvider {
    override fun getToken(onToken: (String) -> Unit) = onToken("testouille")
}
// + single<FcmTokenProvider> { FakeFcmTokenProvider() } dans le module de test passé à TestModel
```

Cas du pur JVM : quand les `actual` sont du code JVM identique des deux côtés, il n'y a rien à
déléguer, la déclaration perd simplement `expect`/`actual`. Par exemple `actual typealias KFile = File`
des deux côtés devient, en code commun :

```kotlin
typealias KFile = java.io.File
```

Supprimez `src/jvmMain` une fois vide.

## Étape 5 - androidApplication

`androidApplication` n'est plus un membre mais une propriété d'extension de `tech.skot.core.di`.
Ajoutez dans chaque fichier listé par la section 3 de `detect.sh` :

```kotlin
import tech.skot.core.di.androidApplication
```

## Étape 6 - Générer et vérifier

```bash
./gradlew skot:skGenerate
```

Comparez `generated/` à l'instantané `/tmp/generated-before.tgz` : seuls des imports et
`androidModelFrameworkModule` peuvent changer. Puis :

```bash
./gradlew viewmodel:testDebugUnitTest model:testDebugUnitTest
./gradlew androidApp:assembleDebug
```

Le nombre de tests doit être celui relevé avant de commencer, tous verts.

## Étape 7 - CI

Remplacez les tâches `jvmTest` par `testDebugUnitTest` (`viewmodel:testDebugUnitTest`,
`model:testDebugUnitTest`). Il n'y a pas de tâche `jvmTest` de compatibilité.

## Modules KMP partagés

Gardez `jvm()` sur un module KMP partagé dont dépend un contrat (pldt : `pldtShared` est une
dépendance `api` de `viewcontract`). Les autres cibles `jvm()` ajoutées « pour la compatibilité skot »
peuvent être supprimées. La section 5 de `detect.sh` les liste et indique qui en dépend.

## Bibliothèques SKot

`sk-tabbar`, `sk-map` et `sk-video` continuent de fonctionner : leurs anciennes coordonnées
(`core-jvm`, `viewmodel-android`…) sont substituées par les artefacts 2.0.0-ua. `sk-phonenumber`
appelle `androidApplication` comme un membre : il doit être recompilé sur 2.0.0-ua avant usage.

## Erreurs fréquentes

- `SKot 2.0.0-ua : le module :xxx applique encore kotlin("multiplatform"). Retirez-le ainsi que le bloc
  kotlin { android { } sourceSets { } }` : le script du module n'est pas migré, reprenez l'étape 2.
- `SKot 2.0.0-ua : le module :xxx est un module JVM, déplacez ces fichiers Android dans model` suivi de
  la liste des fichiers : ce sont ceux de la section 2 de `detect.sh`, reprenez l'étape 3.
- `Method … not mocked` dans un test : une API Android est atteinte depuis un test unitaire. Ne
  l'autorisez pas avec `isReturnDefaultValues` ; liez un faux dans l'injecteur de test (étape 4).
