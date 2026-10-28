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

## Flavors (2.0.0.b2-ua)

### Pourquoi

Les environnements et les marques deviennent des product flavors Android standard. La variante se
choisit dans le sélecteur Build Variants, chaque variante se construit sans sync ni régénération, et la
CI lance `assemble<Marque><Env><BuildType>`. `skSwitchTask`, `skot_variants.properties`,
`skBuildSrcVariants`, `skReadVariants` et les dossiers `kotlin<Variante>` / `res<Variante>` /
`generated<Variante>` disparaissent : les modules clients adoptent l'arborescence Android standard
(`src/main`, `src/test`, `src/<flavor>`). Prérequis : le projet est déjà sur SKot 2.0.0 (étapes 1 à 7).
Les bibliothèques SKot (`sk-*`, plugins `tech.skot.library*`) gardent leur arborescence et n'ont pas de
flavors.

### Lancer le script

```bash
scripts/migration/2.0.0-ua/migrate-flavors.sh <racine du projet>           # simulation (par défaut)
scripts/migration/2.0.0-ua/migrate-flavors.sh <racine du projet> --apply   # applique
```

Sans `--apply`, le script affiche le plan : les flavors déduits des appels `skSwitchTask` de
`skot/build.gradle.kts` (la marque `normal` est ignorée), les dossiers déplacés, les suppressions, la
création éventuelle du module `:variant` et la liste de ce qu'il reste à migrer à la main. `--apply`
exige un arbre git propre sous la racine et ne commite rien : committez le résultat seul, c'est un
commit « déplacements uniquement » facile à relire :

```bash
git add -A && git commit -m "CHORE - Move the sources to the standard flavor layout (moves only)"
```

Le script s'arrête (code 1, rien n'est modifié) en listant les causes sur : une collision (même chemin
relatif dans deux sources, par exemple un fichier à la fois dans `commonMain` et `androidMain`), un
suffixe de variante inconnu, un dossier `src/jvmMain` (code JVM à traiter avant), un chemin de module
contenant une espace, une destination qui existe déjà. Corrigez, puis relancez.

Principaux déplacements : `src/commonMain/kotlin` et `src/androidMain/kotlin` vers `src/main/kotlin` ;
`src/jvmTest` et `src/commonTest` vers `src/test/kotlin` ; `kotlinSezaneProd` vers `src/sezaneProd/kotlin` ;
`resSezane` vers `src/sezane/res`, `resSezane_referenced` vers `src/sezane/res_referenced` ; le code
propre à une variante d'un contrat vers `variant/src/<variante>/kotlin`. Les dossiers `generated*` sont
supprimés.

Juste après le script, l'arbre n'a plus de sources générées, et `skGenerate` s'exécute après
`compileKotlin` : lancez d'abord la génération seule, puis construisez.

```bash
./gradlew skot:skGenerate
./gradlew assembleSezaneProdDebug
```

### Déclarer les flavors

Le script écrit `skot_flavors.properties` à la racine du projet (sans appel `skSwitchTask`
trouvé, il ne l'écrit pas : écrivez-le alors à la main) :

```properties
brand=sezane,octobre
environment=qa,preprod,prod
```

- `environment` est obligatoire, `brand` est facultatif (absent pour un client sans marques).
- Les dimensions sont `brand` puis `environment` : les variantes s'appellent `sezaneProdDebug`, les
  source sets `src/sezane`, `src/prod`, `src/sezaneProd`.
- La première valeur de chaque dimension est la variante par défaut.
- Les noms sont en lowerCamelCase (lettres et chiffres), uniques sur les deux dimensions, et différents
  des noms réservés `main`, `test`, `androidTest`, `testFixtures`, `debug`, `release`.

Le fichier est lu par tous les plugins SKot et par le générateur : les flavors sont identiques dans
chaque module Android, condition pour qu'AGP associe leurs variantes. Les contrats (`viewcontract`,
`modelcontract`) restent sans flavors.

### `buildSrc` → `productFlavors`

`buildSrc` ne connaît plus les variantes. Retirez de `skot/build.gradle.kts` les appels `skSwitchTask`
et `skBuildSrcVariants`, supprimez les dossiers `buildSrc/src/main/kotlin<Variante>` et le code qui
appelle `skReadVariants`. Les valeurs propres à une variante passent dans `androidApp/build.gradle.kts`
(`applicationId`, `applicationIdSuffix`, placeholders du manifeste comme `app_name`, signature,
`buildConfigField`). Exemple Sezane :

```kotlin
android.productFlavors {
    named("octobre") {
        applicationId = "com.octobre.android"
        manifestPlaceholders["scheme"] = "octobre"
    }
    named("qa") { applicationIdSuffix = ".qanew" }
}
```

Le `google-services.json` de chaque marque va dans `androidApp/src/<flavor>/google-services.json`
(`androidApp/src/sezane/`, `androidApp/src/octobre/`) ; une signature par marque se déclare dans le
bloc du flavor. Supprimez aussi les lignes `referenceIconsByVariant`, `referenceFontsByVariant` et
`referenceColorsByVariant` (voir plus bas).

Points à reprendre aussi dans les scripts Gradle (pilote Sezane) :

- `buildSrc` ne garde que ce qui est commun à toutes les variantes : un `SKBuild` réduit à `versionName`,
  la lecture de la signature. La fonction qui copiait le `google-services.json` de la marque
  (`copyGoogleServicesJsonFile`) disparaît, le fichier de chaque marque est déjà dans `src/<flavor>/`.
  Les fichiers `buildSrc/src/main/kotlin<Variante>/google-services.json` se déplacent par `git mv`
  ou se recréent à partir de `git show` avant de supprimer le dossier.
- Un libellé qui combinait marque, environnement et type de build (`D_qa_sezane_Sézane`) se recalcule
  dans `androidComponents.onVariants { variant -> variant.manifestPlaceholders.put("app_name", …) }`,
  avec `variant.productFlavors` (paires dimension/valeur) et `variant.buildType`.
- Les placeholders (`scheme`, `web_site_host`) et les `buildConfigField` du module `view` passent dans
  `android.productFlavors.named("<marque>") { … }` de ce module, comme pour `androidApp`.
- Un identifiant de distribution par marque (`firebaseAppDistribution { serviceCredentialsFile = … }`)
  se place dans le bloc `named("<marque>")` du flavor.
- `base.archivesName` est unique pour tout le module : le nom de fichier du APK ne peut plus porter la
  marque ni l'environnement (AGP ajoute `-<flavor>-<buildType>`), par exemple
  `androidApp-v1.8.3(781)-sezane-qa-debug.apk`. Les dossiers de sortie sont
  `androidApp/build/outputs/apk/<sezaneQa>/<buildType>` et `bundle/<sezaneQa><BuildType>`.
- Un module Android du projet qui n'est pas un module SKot mais dépend d'un module à flavors (cas
  `adyendropin` chez Sezane, qui dépend de `:model`) échoue avec « The only attribute distinguishing these
  variants is VariantAttr ». Remplacez son `id("com.android.library")` par `id("tech.skot.variant")` : il
  reçoit les mêmes flavors.
- Si un `.gitignore` ignorait `modelcontract/generated/commonMain/kotlin/<package>/SKBuild.kt`, remplacez
  le chemin par `modelcontract/generated/main/kotlin/<package>/SKBuild.kt`.

### Module `:variant`

Un contrat est un module JVM sans flavors : il ne peut plus porter de code propre à une variante
(`TargetConstants` par marque et environnement, `OperationJackpotConfig` par marque, `EnvironmentConstants`
chez Hedios). Ce code va dans un module Android `:variant`, créé par le script quand il a déplacé du code
de contrat (`variant/build.gradle.kts`, `src/main/AndroidManifest.xml`, `include(":variant")`) :

```kotlin
plugins {
	id("tech.skot.variant")
}

android {
	namespace = "com.sezane.variant"
}
```

Le plugin `tech.skot.variant` déclare les mêmes flavors et l'arborescence standard, sans génération.
Dès que `:variant` existe, `viewmodel`, `model` et `viewlegacy` ajoutent `api(project(":variant"))`,
`feature` et `app` ajoutent `implementation(project(":variant"))` : les appels du code client ne changent pas. Les fichiers se rangent par
variante : `variant/src/sezaneProd/kotlin/…`, `variant/src/octobre/kotlin/…`. Les valeurs de `SKBuild`
lues à l'exécution par ce code vont aussi dans `:variant`.

Le `SKBuild` généré dans `modelcontract` ne contient plus que les valeurs communes : tout ce que le code
lit à l'exécution par marque ou par environnement (`SKBuild.scheme`, `SKBuild.appName`,
`SKBuild.currentEnvironment`) devient un objet de `:variant` (chez Sezane `BrandBuild` dans
`variant/src/<marque>/kotlin/com/sezane/` et `Environment` dans `variant/src/<env>/kotlin/…`), et les
appels sont remplacés. Une constante de contrat construite sur ces valeurs (`Constants.successConnectionRedirect
= "${SKBuild.scheme}://…"`) se déplace aussi dans `:variant`, un contrat ne pouvant pas le lire. Si le
code déplacé par le script utilise des types d'un contrat (`JackpotCode` de `modelcontract`), ajoutez dans
`variant/build.gradle.kts` `dependencies { api(project(":modelcontract")) }` (un contrat ne dépend jamais
de `:variant`, pas de cycle).

### Ressources par marque

Les chaînes, couleurs, polices et icônes référencées sont l'union de `src/main/res_referenced` et des
`res_referenced` de chaque variante du module `view` : une seule interface (`viewcontract/generated/main/kotlin`),
une seule implémentation (`view/generated/main/kotlin`, `app` pour les icônes). Les marques déclarent donc les
mêmes noms (`logo_demain`, `icn_history`…) ; `R.drawable.x` se résout par variante à la fusion des
ressources. Les dimens, styles et pluriels sont encore lus dans `src/main/res_referenced` seulement.
Les générés sont écrits dans `generated/main/kotlin`, `generated/test/kotlin` et
`generated/androidTest/kotlin`.

Le générateur s'arrête si une ressource de l'union manque dans une variante, en nommant la ressource et
les variantes concernées (voir « Erreurs du build »). Supprimez les lignes `referenceXxxByVariant`.

### CI

| Avant | Après |
|---|---|
| `./gradlew skot:SezaneProd assembleRelease` | `./gradlew skot:skGenerate`, puis `./gradlew assembleSezaneProdRelease` |
| `./gradlew test` ou `testDebugUnitTest` | `./gradlew testSezaneQaDebugUnitTest` |

`skGenerate` s'exécute après `compileKotlin` : sur un arbre sans sources générées (juste après le
script, ou checkout de CI si le projet ne commite pas `generated/`), `./gradlew skot:skGenerate` doit
être lancé seul, dans sa propre commande, avant `./gradlew assemble<Marque><Env><BuildType>` ; les deux
dans une même invocation échouent.
Après la génération, committez les dossiers `generated/` si le projet les versionne (ils étaient supprimés
par le script). Les résultats de test sont dans `<module>/build/test-results/test<Variante>DebugUnitTest`
(plus `testDebugUnitTest`) : à reporter dans les `store_test_results` de la CI. Une sortie CI ne peut pas
interpoler de variable d'environnement dans `store_artifacts` : donnez le dossier parent
`androidApp/build/outputs/apk` (un seul flavor y est construit). Dans les paramètres de CI en
majuscules (`QA`, `PreProd`), les noms de tâches s'écrivent `Qa`, `Preprod` : `assembleSezaneQaRelease`,
`bundleOctobrePreprodRelease`, `appDistributionUploadSezaneQaRelease`.
Un `test` sans variante lance les tests de toutes les variantes (6 fois chez Sezane) : testez une seule
variante, `test<Marque><Env>DebugUnitTest`. Le script liste les appels `skot:<Tâche>` qu'il trouve dans
les fichiers de CI (`.yml`, `.sh`, `Fastfile`, `Jenkinsfile`) : à remplacer à la main.

### Erreurs du build

- `SKot 2.0.0.b2-ua : le module :model utilise encore l'ancienne arborescence SKot. Lancez
  scripts/migration/2.0.0-ua/migrate-flavors.sh (dépôt SKot) (voir docs/migration/migration-2.0.0-ua.md)`
  suivi de la liste des éléments trouvés : `skot_variants.properties` à la racine, un dossier
  `src/commonMain`, `androidMain`, `jvmMain`, `jvmTest`, `commonTest`, `generated/commonMain`,
  `generated/androidMain`, `generated/jvmTest` ou `generated<Variante>`. Lancez le script.
- `SKot 2.0.0.b2-ua : le module :viewcontract est un contrat sans variantes, déplacez ces fichiers dans
  le module :variant (voir docs/migration/migration-2.0.0-ua.md)` suivi des fichiers : un contrat contient
  un dossier `src/<autre que main et test>`. Déplacez-les dans `variant/src/<variante>/kotlin`.
- `SKot 2.0.0.b2-ua : skot_flavors.properties : …` : fichier invalide, parmi « la propriété environment
  doit lister au moins un environnement », « « X » n'est pas un nom de flavor valide (lowerCamelCase,
  lettres et chiffres) », « « X » est un nom réservé par Android Gradle Plugin », « flavors déclarés
  plusieurs fois : X ».
- `Ressources string référencées absentes de certaines variantes :` (idem avec `color`, `font` et
  `drawable`) suivi de lignes `  <variante> : <ressource>, …` : ajoutez la ressource dans
  `src/<flavor>/res_referenced` des marques ou environnements concernés, ou dans `src/main/res_referenced`
  si elle est commune.

## Modules KMP partagés

Gardez `jvm()` sur un module KMP partagé dont dépend un contrat (pldt : `pldtShared` est une
dépendance `api` de `viewcontract`). Les autres cibles `jvm()` ajoutées « pour la compatibilité skot »
peuvent être supprimées. La section 5 de `detect.sh` les liste et indique qui en dépend.

## Bibliothèques SKot

`sk-tabbar` et `sk-map` continuent de fonctionner (validé par le pilote) : leurs anciennes coordonnées
(`core-jvm`, `viewmodel-android`…) sont substituées par les artefacts 2.0.0-ua. `sk-video` n'a pas été
exercée par le pilote (elle a ensuite été migrée et construite, sans être exercée dans une application
cliente). `sk-phonenumber`
appelle `androidApplication` comme un membre : il doit être recompilé sur 2.0.0-ua avant usage.

## Migrer une bibliothèque SKot

### Quand est-ce nécessaire

Une bibliothèque déjà publiée (`sk-tabbar`, `sk-video`, `sk-scanner`, `sk-phonenumber`, `sk-map`)
continue de fonctionner avec SKot 2.0.0-ua : la compatibilité binaire et la substitution des anciennes
coordonnées (`core-jvm`, `viewmodel-android`…) vers les artefacts 2.0.0-ua suffisent. Il n'y a donc
rien à faire tant qu'on ne la reconstruit pas. En revanche, **une bibliothèque doit être migrée avant
d'être recompilée** : ses scripts KMP sont refusés par les plugins 2.0.0-ua. `sk-phonenumber` doit être
recompilée (sa migration est faite sur `chore/skot-2.0.0-ua`), car elle appelle `androidApplication` comme un membre (étape 5).

Une bibliothèque se migre seule, indépendamment des applications qui l'utilisent : ces dernières ne
changent que sa version.

Les cinq bibliothèques ont été migrées sur des branches locales `chore/skot-2.0.0-ua` (pas encore
publiées) : la procédure ci-dessous en rassemble les enseignements. Pour
recopier un fichier (wrapper, `gradle-daemon-jvm.properties`…), prenez une bibliothèque déjà migrée
comme référence (ex. `sk-map`).

### Prérequis et environnement

- **Java 21 pour le démon Gradle.** Si `gradle/gradle-daemon-jvm.properties` fixe Java 17, la commande
  `./gradlew wrapper` échoue avec « Dependency requires at least JVM runtime version 21 » : copiez
  d'abord le fichier d'une bibliothèque migrée (`./gradlew updateDaemonJvm` ne fonctionne qu'après le
  passage du wrapper à 9.7.1 : sous l'ancien Gradle et les anciens scripts, il échoue comme toute tâche).
- **Mémoire de Gradle.** Dans `gradle.properties`, prévoyez par exemple
  `org.gradle.jvmargs=-Xmx4096m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8` (recommandé) :
  les défauts (512 Mio de tas, 384 Mio de metaspace) peuvent ne plus suffire avec Gradle 9.7.1.
- **`mavenLocal()`.** Tant que `skot:plugin:2.0.0-ua` n'est pas sur JitPack, `mavenLocal()` doit être
  actif dans les dépôts de `buildscript` **et** de `allprojects` (sinon : « Could not find …
  plugin:2.0.0-ua »). Publiez d'abord le framework en local.

### Version

Montez la version **majeure** de la bibliothèque et suffixez avec la version du framework, par exemple
`sk-map` `2.0.1_1.5.5-ua` → `3.0.0_2.0.0-ua`. Dans `buildSrc/src/main/kotlin/Versions.kt` :

```kotlin
const val library = "3.0.0"      // était 2.0.1
const val framework = "2.0.0-ua" // était 1.5.5-ua
```

### Wrapper Gradle 9.7.1

AGP 9.4.0 exige Gradle 9.6.0 ou plus. **Ne lancez pas `./gradlew wrapper` sous l'ancien Gradle** tant
que les scripts sont ceux de KMP : avec le plugin AGP 9 sur le classpath et les anciens scripts, il
échoue (`NoClassDefFoundError … ProjectTypeBinding`), et même quand il passe, il ne fait que changer la
version en gardant l'ancien jar et les anciens scripts du wrapper. Ordre recommandé :

1. Copiez `gradle/gradle-daemon-jvm.properties` (Java 21) d'une bibliothèque migrée (voir plus haut).
2. Copiez `gradle/wrapper/gradle-wrapper.properties` d'une bibliothèque migrée (`distributionUrl` en
   9.7.1 et `distributionSha256Sum` : la validation du checksum refuse sinon la nouvelle distribution),
   ou éditez-le à la main. Évitez `sed -i` : sur macOS il exige `sed -i ''`.
3. Migrez les scripts des modules (section suivante) : `./gradlew wrapper` configure le projet, et les
   scripts KMP sont refusés par le garde-fou du plugin.
4. Régénérez le wrapper, une seule fois, sous 9.7.1 :

   ```bash
   ./gradlew wrapper --gradle-version 9.7.1 --gradle-distribution-sha256-sum acd53f1edaf02f1a8ff99879f8a34b302661a057d9b063ae9e35b552f804d20a
   ```

   Cette exécution régénère `gradle-wrapper.jar`, `gradlew` et `gradlew.bat` (et ajoute les reprises
   `retries` / `retryBackOffMs`).
5. Comparez le jar avec celui d'une bibliothèque migrée :
   `cmp gradle/wrapper/gradle-wrapper.jar <bibliotheque-migree>/gradle/wrapper/gradle-wrapper.jar`.

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

Les sources restent dans `src/jvmMain/kotlin`, le plugin les ajoute au source set `main`. Les
dépendances propres à la bibliothèque restent dans ce module (sk-phonenumber :
`implementation("com.googlecode.libphonenumber:libphonenumber:9.0.33")`). Une bibliothèque peut aussi
avoir à **créer** ce module (sk-phonenumber : l'ancienne implémentation `jvmMain` y a été déplacée), et
pas seulement à remplacer un ancien module KMP `jvm()`. Le module est publié en AAR : il n'est consommable que par les tests unitaires Android des applications.

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

### Cas particuliers

**Bibliothèque sans module `:viewcontract`.** Un module qui ne peut pas utiliser `tech.skot.library`
(pas de contrat dont dépendre) se déclare à la main (extrait de `viewmodel/build.gradle.kts` de
sk-phonenumber) :

```kotlin
import tech.skot.tools.gradle.androidBaseConfig

plugins {
    id("com.android.library")
    id("maven-publish")
    signing
}

android {
    namespace = "tech.skot.libraries.phonenumber.viewmodel"
    androidBaseConfig(project)

    sourceSets {
        getByName("main") {
            kotlin {
                directories.add("src/commonMain/kotlin")
                directories.add("src/androidMain/kotlin")
            }
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
    }

    publishing {
        singleVariant("release") { withSourcesJar() }
    }
}

dependencies {
    implementation("${tech.skot.Versions.group}:viewmodel:${tech.skot.Versions.skot}")
    // … dépendances propres à la bibliothèque
}

// Avant 2.0.0, Kotlin Multiplatform créait la publication : elle est maintenant déclarée ici.
afterEvaluate {
    publishing {
        publications.create<MavenPublication>("release") {
            from(components["release"])
        }
    }
}
```

Le bloc `if (!localPublication) { … }` existant (pom, `javadocJar`, `signing`) reste tel quel.
`id("maven-publish")` peut être retiré si le script racine l'applique à tous les modules (`allprojects`),
comme dans les autres bibliothèques.

**Dépendances de `viewmodelTests`.** Le plugin câble `project(":viewmodel")`, `viewmodelTests` du
framework et `core`. En revanche, les tests qui utilisent des types du `viewmodel` ou du `viewcontract`
de SKot (`SKTestViewModel`, `CoreViewInjector`, `SKCombo`…) doivent déclarer eux-mêmes
`testImplementation("${tech.skot.Versions.group}:viewmodel:${tech.skot.Versions.skot}")` (l'ancien
`jvmTest` les voyait via `commonMain`).

**Tests instrumentés de la bibliothèque** (`androidTest` du `viewlegacy`) :

- Un test laissé dans le dossier KMP `src/androidInstrumentedTest` est **ignoré sans erreur** après la
  migration : `compileDebugAndroidTestKotlin` passe en `NO-SOURCE` et `assembleDebugAndroidTest` reste
  vert. Déplacez-le (`git mv … src/androidTest`) et vérifiez que la tâche de compilation n'est plus
  `NO-SOURCE`.
- Compilez-les avec `viewlegacy:assembleDebugAndroidTest`. Ils étaient parfois cassés avant la
  migration (sk-tabbar : `Color(res)`, supprimé depuis que `Color` est une interface → `ColorRef(res)` ;
  sk-video : `Track(url = …)` → `Track(uri = { … })`) : corrigez-les dans la migration.
- Un test qui utilise l'injecteur : `import tech.skot.core.di.*` fournit déjà `BaseInjector(app, modules)`
  et `androidApplication`, sans cast sur `ApplicationProvider.getApplicationContext()`.

### Constructions propres à Gradle 8

Elles peuvent casser sous 9.7.1 :

- `buildDir` (ex. `"$buildDir/dokka"`) : casse à coup sûr. Utilisez `layout.buildDirectory.dir("dokkaHtml")`.
- **dokka 1.x** (classpath 1.5.30, `tasks.dokkaHtmlPartial`, `dokkaGfmPartial`) : peut casser (sk-scanner).
  Passez alors à dokka 2.2.0, comme sk-map, sk-scanner et sk-phonenumber ; c'est facultatif sinon :
  `sk-video` (dokka 1.5.30 et tâches `*Partial`) et `sk-tabbar` (dokka 1.9.10, `DokkaTaskPartial`)
  construisent sous Gradle 9.7.1 sans changement. Pour sk-video, le `javadocJar` n'est de toute façon
  pas publié ; garder dokka 1 a pour coût une configuration dokka différente d'une bibliothèque à l'autre.

  Dans le script racine, montez le classpath du `buildscript` :
  `classpath("org.jetbrains.dokka:dokka-gradle-plugin:2.2.0")` (sk-map :
  `classpath(libs.dokka.gradle.plugin)` avec `dokkaGradlePlugin = "2.2.0"` dans `libs.versions.toml`) ;
  le `apply(plugin = "org.jetbrains.dokka")` de `allprojects` reste. Dans chaque module :

  ```kotlin
  val dokkaOutputDir = layout.buildDirectory.dir("dokkaHtml")

  dokka {
      moduleName.set("sk-phonenumber")
      dokkaPublications.html {
          outputDirectory.set(dokkaOutputDir)
      }
      dokkaSourceSets.configureEach {
          suppressInheritedMembers.set(true) // équivalent de suppressInheritedMembers en dokka 1
      }
  }

  val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") {
      delete(dokkaOutputDir)
  }

  val javadocJar = tasks.register<Jar>("javadocJar") {
      dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
      archiveClassifier.set("javadoc")
      from(dokkaOutputDir)
  }
  ```

  Les blocs `*Partial` de dokka 1 n'ont jamais agi sur la javadoc publiée.

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

### Finitions

- `jitpack.yml` : `openjdk17` → `openjdk21`.
- `.gitignore` : ajoutez `/buildSrc/.kotlin`. Si le fichier ne se termine pas par un retour à la ligne,
  ajoutez-le d'abord, sinon la ligne s'accole à la précédente (`/reports/buildSrc/.kotlin`).
- `src/androidMain/AndroidManifest.xml` : vous pouvez retirer l'attribut `package=` (AGP avertit qu'il
  est ignoré). Facultatif.
- `id("maven-publish")` dans les scripts des modules : redondant, vous pouvez le retirer (sk-map,
  sk-scanner et sk-video l'ont retiré ; sk-tabbar et sk-phonenumber l'ont gardé, les deux fonctionnent).

### Vérifier

1. Construisez la bibliothèque, puis publiez-la en local. La publication se lance sans les tests ni les
   vérifications, ce qui est volontaire (ils sont déjà passés au `build`) :

   ```bash
   ./gradlew clean build -x lint
   ./gradlew publishToMavenLocal -x test -x check
   ```

2. Contrôlez `~/.m2/repository/<groupe>/<module>/<version>/` (ex. `3.0.0_2.0.0-ua`) : le contrat
   `viewcontract` donne un `.jar`, `viewmodel`, `viewmodelTests` et `viewlegacy` un `.aar`, chacun avec
   son `-sources.jar`, son `.module` et son `.pom`. Il ne doit y avoir ni `-jvm` ni `-android`, et
   aucun `core-jvm` dans les dépendances du `.module`.
3. Dans une application cliente migrée (étapes 1 à 7), montez la bibliothèque dans
   `skot_librairies.properties` (ex. `sk-map` → `3.0.0_2.0.0-ua`), puis lancez :

   ```bash
   ./gradlew skot:skGenerate
   ./gradlew viewmodel:testDebugUnitTest model:testDebugUnitTest --rerun-tasks
   ./gradlew androidApp:assembleDebug
   ```

   `--rerun-tasks` donne les vrais nombres de tests (sans lui, les tâches à jour n'en comptent aucun).
   Les tests doivent être aussi nombreux et verts que dans l'état de référence. Contrôlez que la
   version ancienne de la bibliothèque et `core-jvm` ont disparu du classpath
   (`./gradlew androidApp:dependencies --configuration debugRuntimeClasspath`).
4. `skGenerate` réécrit l'horodatage de l'export de `strings.xml` : annulez ce changement
   (`git checkout -- <strings.xml>`) avant de committer.
5. **Tests déjà cassés avant la migration** (ex. sk-phonenumber : `isValid` a gagné un paramètre
   `phoneType`) : corrigez-les temporairement pour obtenir un compte de référence, et gardez la
   correction dans la migration.
6. Pour lancer l'application cliente Argos, démarrez `RootActivity` explicitement
   (`am start -n <package>/org.protection_civile.argos.android.RootActivity`, avec le suffixe de package
   preprod) : LeakCanary ajoute une icône de lancement.

### Publication sur Maven Central

Les modules `viewmodelTests` sont publiés sans personnalisation du pom ni de la signature. C'est aussi
le cas de tous les modules de sk-map et de sk-scanner (personnalisation retirée depuis longtemps dans
sk-map, commit `c5fe88d`) ; seul le `viewmodel` de sk-phonenumber garde son bloc pom/signature. Les
bibliothèques sont distribuées par JitPack, où cela suffit ; c'est une limite pour Maven Central
seulement : une future publication Central de ces artefacts serait refusée tant que le pom et la
signature ne sont pas complétés.

### Checklist

- [ ] Java 21 dans `gradle-daemon-jvm.properties`, `org.gradle.jvmargs` augmenté, `mavenLocal()` actif
- [ ] Version `X+1.0.0_2.0.0-ua` dans `Versions.kt`
- [ ] Wrapper 9.7.1 : propriétés copiées, scripts migrés, `./gradlew wrapper …` sous 9.7.1, `cmp` du jar
- [ ] `kotlin("multiplatform")` et blocs `kotlin { … }` retirés des modules, namespaces déplacés dans `android { }`
- [ ] Plugins `tech.skot.library-contract`, `library`, `library-viewmodeltests`, `library-viewlegacy`
- [ ] `compileSdk` < 37, `kotlin.mpp.*`, entrées `skot-framework` retirés
- [ ] `core-jvm` et variantes KMP remplacés par les coordonnées racines
- [ ] Publications et sources jars manuels retirés
- [ ] `testImplementation` du `viewmodel` SKot dans `viewmodelTests` si ses tests en utilisent les types
- [ ] Tests instrumentés déplacés vers `src/androidTest`, compilation pas en `NO-SOURCE`
- [ ] `buildDir` et dokka 1 remplacés (ou dokka 1.5.30 conservé en connaissance de cause)
- [ ] `jitpack.yml` en `openjdk21`, `.gitignore` complété (retour à la ligne final)
- [ ] `androidApplication` importé depuis `tech.skot.core.di` si utilisé (`sk-phonenumber`)
- [ ] `build -x lint`, `publishToMavenLocal -x test -x check`, artefacts de `~/.m2` contrôlés
- [ ] Application cliente : tests (`--rerun-tasks`) et `assembleDebug` verts, horodatage `strings.xml` annulé

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
