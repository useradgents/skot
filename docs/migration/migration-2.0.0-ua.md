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

Autres réglages du bloc `kotlin { }` :

- `jvmToolchain(21)` : les plugins des contrats le posent eux-mêmes. Dans `viewmodel` et `model`, un
  `jvmToolchain(21)` déclaré dans l'ancien bloc `kotlin { }` se garde tel quel dans un bloc
  `kotlin { jvmToolchain(21) }` de premier niveau.
- `androidResources { enable = true }` (bloc `android { }` de KMP) disparaît : les ressources
  Android sont activées par défaut dans un module `com.android.library`.

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
- Exemple pldt : `modelcontract/src/androidMain/kotlin/FirebaseRemoteConfigImpl.kt` va dans
  `model/src/androidMain/kotlin/FirebaseRemoteConfigImpl.kt`.

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

Points d'attention :

- `commonMain` et `androidMain` sont maintenant compilés ensemble : deux fichiers de même nom dans le
  même package qui ont tous deux des déclarations de premier niveau (fonction, propriété, `const`)
  produisent deux classes `<Nom>Kt` et la compilation échoue (`Duplicate JVM class name`). Le délégué
  reste dans le fichier commun ; dans le fichier Android, gardez seulement la classe
  d'implémentation et rangez ses constantes privées dans un `private companion object`.
- Un ancien `actual` `jvmMain` vide (« rien à faire en test ») ne l'est plus : sans faux lié,
  `get<Interface>()` échoue dans tous les tests qui atteignent le délégué. Liez les faux dans **chaque**
  injecteur de test, y compris ceux qu'une classe de base recrée dans un `@Before`
  (`injector = BaseInjector(listOf(modelFrameworkModule, …))`). Un module de faux unique, ajouté aux
  modules de `SKTestModel(…)` et à ces listes, suffit.
- Les implémentations Android qui ont besoin d'un `Context` le reçoivent au constructeur, lié avec
  `single<Interface> { AndroidImpl(androidApplication) }` : ce fichier a lui aussi besoin de l'import
  de l'étape 5.

## Étape 5 - androidApplication

`androidApplication` n'est plus un membre mais une propriété d'extension de `tech.skot.core.di`.
Ajoutez dans chaque fichier listé par la section 3 de `detect.sh` (y compris ceux des dossiers de
variantes inactives, comme `kotlinMock`, que la compilation courante ne vérifie pas), ainsi que dans
les modules DI où vous liez les implémentations de l'étape 4 :

```kotlin
import tech.skot.core.di.androidApplication
```

Le module de DI du framework côté Android s'appelle désormais `androidModelFrameworkModule`
(`tech.skot.di`). Le code écrit à la main qui importait `tech.skot.di.modelFrameworkModule`
(`androidMain`, dossiers de variantes, injecteurs de features, `Application` personnalisée) doit passer à
`androidModelFrameworkModule` ; la section 6 de `detect.sh` les liste. Les tests gardent
`modelFrameworkModule` (fourni par `modelTests`).

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

`sk-tabbar` et `sk-map` continuent de fonctionner (validé par le pilote) : leurs anciennes coordonnées
(`core-jvm`, `viewmodel-android`…) sont substituées par les artefacts 2.0.0-ua. `sk-video` n'a pas été
exercée par le pilote. `sk-phonenumber`
appelle `androidApplication` comme un membre : il doit être recompilé sur 2.0.0-ua avant usage.

## Migrer une bibliothèque SKot

### Quand est-ce nécessaire

Une bibliothèque déjà publiée (`sk-tabbar`, `sk-video`, `sk-scanner`, `sk-phonenumber`, `sk-map`)
continue de fonctionner avec SKot 2.0.0-ua : la compatibilité binaire et la substitution des anciennes
coordonnées (`core-jvm`, `viewmodel-android`…) vers les artefacts 2.0.0-ua suffisent. Il n'y a donc
rien à faire tant qu'on ne la reconstruit pas. En revanche, **une bibliothèque doit être migrée avant
d'être recompilée** : ses scripts KMP sont refusés par les plugins 2.0.0-ua. `sk-phonenumber` doit être
recompilée dès maintenant, car elle appelle `androidApplication` comme un membre (étape 5).

Une bibliothèque se migre seule, indépendamment des applications qui l'utilisent : ces dernières ne
changent que sa version.

### Version

Montez la version **majeure** de la bibliothèque et suffixez avec la version du framework, par exemple
`sk-map` `2.0.1_1.5.5-ua` → `3.0.0_2.0.0-ua`. Dans `buildSrc/src/main/kotlin/Versions.kt` :

```kotlin
const val library = "3.0.0"      // était 2.0.1
const val framework = "2.0.0-ua" // était 1.5.5-ua
```

### Gradle 9.6 minimum

AGP 9.4.0 exige Gradle 9.6.0 ou plus. Si le wrapper de la bibliothèque est plus ancien, **éditez
d'abord `gradle/wrapper/gradle-wrapper.properties` à la main** (`distributionUrl` et
`distributionSha256Sum` : la validation du checksum refuse sinon la nouvelle distribution), puis
régénérez le wrapper :

```bash
./gradlew wrapper --gradle-version 9.7.1 --gradle-distribution-sha256-sum acd53f1edaf02f1a8ff99879f8a34b302661a057d9b063ae9e35b552f804d20a
```

### Scripts des modules

Les modules de bibliothèque utilisent leurs propres plugins : `kotlin("multiplatform")` et les blocs
`kotlin { … }` KMP disparaissent. `compileSdk` et `minSdk` sont fournis par le plugin.

`viewcontract` : `tech.skot.library-contract`

```kotlin
// Avant
plugins {
    kotlin("multiplatform")
    id("tech.skot.library-contract")
    signing
}

// Après
plugins {
    id("tech.skot.library-contract")
    signing
}
```

`viewmodel` : `tech.skot.library`, avec `namespace` dans `android { }`

```kotlin
// Avant
plugins {
    kotlin("multiplatform")
    id("tech.skot.library")
    signing
}
kotlin {
    android {
        compileSdk = 36
        namespace = "tech.skot.libraries.skmap"
    }
}

// Après
plugins {
    id("tech.skot.library")
    signing
}
android {
    namespace = "tech.skot.libraries.skmap"
}
```

`viewmodelTests` : `tech.skot.library-viewmodeltests`, qui remplace entièrement l'ancien module KMP
`jvm()` (source set, dépendances et publication). Il reste à déclarer le namespace :

```kotlin
// Avant
plugins {
    kotlin("multiplatform")
    signing
}
kotlin {
    jvm("jvm")
    sourceSets {
        val jvmMain by getting {
            kotlin.srcDir("src/jvmMain/kotlin")
            dependencies {
                implementation(project(":viewmodel"))
                implementation("${Versions.frameworkGroup}:viewmodelTests:${Versions.framework}")
                implementation("${Versions.frameworkGroup}:core-jvm:${Versions.framework}")
            }
        }
    }
}

// Après
plugins {
    id("tech.skot.library-viewmodeltests")
    signing
}
android {
    namespace = "tech.skot.libraries.skmap.viewmodeltests"
}
```

Les sources restent dans `src/jvmMain/kotlin`, le plugin les ajoute au source set `main`. Le module est
publié en AAR : il n'est consommable que par les tests unitaires Android des applications.

`viewlegacy` (et ses variantes, ex. `viewlegacy_mapbox`) : `tech.skot.library-viewlegacy`

```kotlin
// Avant
plugins {
    kotlin("multiplatform")
    id("tech.skot.library-viewlegacy")
    signing
}
android {
    namespace = "tech.skot.libraries.skmap.viewlegacy"
}
kotlin {
    jvmToolchain(17)
    androidTarget("android") { }
}

// Après
plugins {
    id("tech.skot.library-viewlegacy")
    signing
}
android {
    namespace = "tech.skot.libraries.skmap.viewlegacy"
}
```

Pour chaque module, ne gardez que ce que les plugins ne font pas : `dokka`, `javadocJar`, `signing` et
les dépendances propres à la bibliothèque (ex. `api("com.mapbox.maps:android:11.0.0")`).

### Pièges rencontrés avec le pilote sk-map

- **`compileSdk` inférieur à 37.** Supprimez tout `compileSdk { version = release(36) }` ou
  `compileSdk = 36` : les AAR 2.0.0-ua exigent 37, et le plugin le fournit.
- **Propriétés `kotlin.mpp.*`.** Retirez `kotlin.mpp.androidSourceSetLayoutVersion=2` (et ses
  semblables) de `gradle.properties`.
- **Entrées `skot-framework` périmées.** Supprimez du `gradle/libs.versions.toml` les versions
  `skot-framework` restées d'avant (sk-map en avait une en `1.4.2-ua`, inutilisée) : elles ne sont plus
  lues et induisent en erreur.
- **`core-jvm` devient `core`.** Toute dépendance écrite à la main sur `core-jvm`, `viewmodel-android`
  ou autre variante KMP doit viser les coordonnées racines (`core`, `viewmodel`, `viewlegacy`…). Seules
  les coordonnées racines existent en 2.0.0-ua : la substitution ne sert qu'aux bibliothèques
  publiées non encore migrées.
- **Sources jars et publications.** Les plugins déclarent déjà le sources jar
  (`singleVariant("release") { withSourcesJar() }`) et la publication (`release`, ou `maven` pour le
  contrat). Supprimez les `publishing { singleVariant … }` et les `afterEvaluate { publishing { … } }`
  écrits à la main : ils feraient doublon. Un module qui n'utilise pas le plugin SKot (ex. un
  `viewmodelTests` recopié à la main) doit, lui, tout déclarer : `sourceSets { getByName("main") {
  kotlin { directories.add("src/jvmMain/kotlin") } } }` (la forme abrégée lève un
  `ClassCastException`), `tech.skot.Versions.android_compileSdk` et `android_minSdk` en nom
  pleinement qualifié, Java 21, et `core` et non `core-jvm`. Préférez le plugin.
- **Un seul paramétrage de Java.** Retirez les `jvmToolchain(17)` et `kotlin { … }` hérités : le plugin
  fixe Java 21.

### Vérifier

1. Construisez la bibliothèque, puis publiez-la en local :

   ```bash
   ./gradlew clean build -x lint
   ./gradlew publishToMavenLocal
   ```

2. Contrôlez `~/.m2/repository/<groupe>/<module>/<version>/` (ex. `3.0.0_2.0.0-ua`) : le contrat
   `viewcontract` donne un `.jar`, `viewmodel`, `viewmodelTests` et `viewlegacy` un `.aar`, chacun avec
   son `-sources.jar`, son `.module` et son `.pom`. Il ne doit y avoir ni `-jvm` ni `-android`, et
   aucun `core-jvm` dans les dépendances du `.module`.
3. Dans une application cliente migrée (étapes 1 à 7), montez la bibliothèque dans
   `skot_librairies.properties` (ex. `sk-map` → `3.0.0_2.0.0-ua`), puis lancez :

   ```bash
   ./gradlew skot:skGenerate
   ./gradlew viewmodel:testDebugUnitTest model:testDebugUnitTest
   ./gradlew androidApp:assembleDebug
   ```

   Les tests doivent être aussi nombreux et verts que dans l'état de référence. Contrôlez que la
   version ancienne de la bibliothèque et `core-jvm` ont disparu du classpath
   (`./gradlew androidApp:dependencies --configuration debugRuntimeClasspath`).
4. `skGenerate` réécrit l'horodatage de l'export de `strings.xml` : annulez ce changement
   (`git checkout -- <strings.xml>`) avant de committer.

### Checklist

- [ ] Wrapper Gradle 9.6 ou plus (propriétés éditées à la main, puis `./gradlew wrapper …`)
- [ ] Version `X+1.0.0_2.0.0-ua` dans `Versions.kt`
- [ ] `kotlin("multiplatform")` et blocs `kotlin { … }` retirés des modules, namespaces déplacés dans `android { }`
- [ ] Plugins `tech.skot.library-contract`, `library`, `library-viewmodeltests`, `library-viewlegacy`
- [ ] `compileSdk` < 37, `kotlin.mpp.*`, entrées `skot-framework` retirés
- [ ] `core-jvm` et variantes KMP remplacés par les coordonnées racines
- [ ] Publications et sources jars manuels retirés
- [ ] `androidApplication` importé depuis `tech.skot.core.di` si utilisé (`sk-phonenumber`)
- [ ] `build -x lint`, `publishToMavenLocal`, artefacts de `~/.m2` contrôlés
- [ ] Application cliente : tests et `assembleDebug` verts, horodatage `strings.xml` annulé

## Erreurs fréquentes

- `SKot 2.0.0-ua : le module :xxx applique encore kotlin("multiplatform"). Retirez-le ainsi que le bloc
  kotlin { android { } sourceSets { } }` : le script du module n'est pas migré, reprenez l'étape 2.
- `SKot 2.0.0-ua : le module :xxx est un module JVM, déplacez ces fichiers Android dans model` suivi de
  la liste des fichiers : ce sont ceux de la section 2 de `detect.sh`, reprenez l'étape 3.
- `Method … not mocked` dans un test : une API Android est atteinte depuis un test unitaire. Ne
  l'autorisez pas avec `isReturnDefaultValues` ; liez un faux dans l'injecteur de test (étape 4).
- `Method … not mocked` pour `formatPhoneNumber` (`PhoneNumberUtils`) ou `toSKUri` (`android.net.Uri`) :
  ces fonctions atteignent l'API Android et ne sont pas utilisables dans un test unitaire local. Liez un
  faux ou évitez de les appeler depuis les tests.
