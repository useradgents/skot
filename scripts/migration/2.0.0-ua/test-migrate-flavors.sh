#!/usr/bin/env bash
# Tests du script de migration vers les flavors, sur des projets SKot miniatures dans des dépôts git temporaires.
set -euo pipefail
script="$(cd "$(dirname "$0")" && pwd)/migrate-flavors.sh"
fail() { echo "ÉCHEC : $*" >&2; exit 1; }
put() { mkdir -p "$(dirname "$1")"; printf '%s\n' "${2:-x}" > "$1"; }
exists() { [ -e "$1" ] || fail "absent : $1"; }
absent() { [ ! -e "$1" ] || fail "présent : $1"; }
new_repo() {
  dir="$(mktemp -d)"
  cd "$dir"
  git init -q
  git config user.email t@t
  git config user.name t
  printf '.DS_Store\n' > .gitignore
}
commit_all() { git add -A && git commit -qm fixture; }

# --- Deux dimensions ----------------------------------------------------------------------------
new_repo
put skot/build.gradle.kts 'skSwitchTask("SezaneQa", "qa", "sezane")
skSwitchTask("SezaneProd", "prod", "sezane")
skSwitchTask("OctobreQa", "qa", "octobre")
skSwitchTask("OctobreProd", "prod", "octobre")
skot { app = App(packageName = "com.example", startScreen = ".A", referenceIconsByVariant = true) }'
put settings.gradle.kts 'include(":viewmodel")'
put skot_variants.properties 'variants=sezane
environment=qa'
put modelcontract/build.gradle.kts 'plugins { id("tech.skot.modelcontract") }'
put modelcontract/src/commonMain/kotlin/com/example/Api.kt
put modelcontract/src/commonMain/kotlinSezaneProd/com/example/TargetConstants.kt
put modelcontract/src/commonMain/kotlinOctobre/com/example/Jackpot.kt
put viewmodel/build.gradle.kts 'plugins { id("tech.skot.viewmodel") }'
put viewmodel/src/commonMain/kotlin/com/example/Vm.kt
put viewmodel/src/androidMain/kotlin/com/example/VmAndroid.kt
put viewmodel/src/androidMain/AndroidManifest.xml '<manifest/>'
put viewmodel/src/jvmTest/kotlin/com/example/VmTest.kt
put viewmodel/src/commonMain/kotlinQa/com/example/Qa.kt
put viewmodel/generated/commonMain/kotlin/com/example/Gen.kt
put viewmodel/generatedSezane/commonMain/kotlin/com/example/IconsGen.kt
put view/build.gradle.kts 'plugins { id("tech.skot.viewlegacy") }'
put view/src/androidMain/res/values/strings.xml
put view/src/androidMain/res_referenced/values/strings.xml
put view/src/androidMain/resOctobre_referenced/drawable/logo.xml
put view/src/androidMain/resSezaneQa/values/x.xml
put buildSrc/src/main/kotlinProd/Build.kt
put .circleci/config.yml '      - run: ./gradlew skot:SezaneProd assembleRelease'
commit_all
put view/src/androidMain/res/.DS_Store

out="$("$script" . 2>&1)"
[ -z "$(git status --porcelain)" ] || fail "la simulation a modifié l'arbre"
case "$out" in *"viewmodel/src/commonMain/kotlin -> viewmodel/src/main/kotlin"*) ;; *) fail "plan sans viewmodel commonMain : $out" ;; esac
case "$out" in *"modelcontract/src/commonMain/kotlinSezaneProd -> variant/src/sezaneProd/kotlin"*) ;; *) fail "plan sans variant : $out" ;; esac
case "$out" in *"buildSrc/src/main/kotlinProd"*) ;; *) fail "buildSrc manquant dans les étapes manuelles" ;; esac
case "$out" in *".circleci/config.yml"*) ;; *) fail "CI manquante dans les étapes manuelles" ;; esac
case "$out" in *"referenceIconsByVariant"*) ;; *) fail "referenceIconsByVariant manquant dans les étapes manuelles" ;; esac

touch dirty.txt
if "$script" . --apply >/dev/null 2>&1; then fail "--apply accepté sur un arbre modifié"; fi
rm dirty.txt

"$script" . --apply >/dev/null
[ "$(cat skot_flavors.properties)" = "brand=sezane,octobre
environment=qa,prod" ] || fail "skot_flavors.properties : $(cat skot_flavors.properties)"
exists modelcontract/src/main/kotlin/com/example/Api.kt
exists variant/src/sezaneProd/kotlin/com/example/TargetConstants.kt
exists variant/src/octobre/kotlin/com/example/Jackpot.kt
exists viewmodel/src/main/kotlin/com/example/Vm.kt
exists viewmodel/src/main/kotlin/com/example/VmAndroid.kt
exists viewmodel/src/main/AndroidManifest.xml
exists viewmodel/src/test/kotlin/com/example/VmTest.kt
exists viewmodel/src/qa/kotlin/com/example/Qa.kt
exists view/src/main/res/values/strings.xml
exists view/src/main/res_referenced/values/strings.xml
exists view/src/octobre/res_referenced/drawable/logo.xml
exists view/src/sezaneQa/res/values/x.xml
exists buildSrc/src/main/kotlinProd/Build.kt
absent skot_variants.properties
absent viewmodel/generated
absent viewmodel/generatedSezane
absent viewmodel/src/commonMain
absent view/src/androidMain
absent modelcontract/src/commonMain
grep -q 'namespace = "com.example.variant"' variant/build.gradle.kts || fail "namespace de :variant"
grep -q 'id("tech.skot.variant")' variant/build.gradle.kts || fail "plugin de :variant"
exists variant/src/main/AndroidManifest.xml
grep -q 'include(":variant")' settings.gradle.kts || fail "include de :variant"
[ "$(git diff --cached -M --name-status | grep -c '^R')" -ge 12 ] || fail "déplacements non enregistrés comme renommages"

# --- Marque factice normal, environnements seuls ------------------------------------------------
new_repo
put skot/build.gradle.kts 'skSwitchTask("Preprod", "preprod", "normal")
skSwitchTask("Prod", "prod", "normal")'
put viewmodel/build.gradle.kts 'plugins { id("tech.skot.viewmodel") }'
put viewmodel/src/commonMain/kotlin/a/A.kt
put viewmodel/src/commonMain/kotlinNormal/a/N.kt
put viewmodel/src/commonMain/kotlinNormalPreprod/a/P.kt
put view/build.gradle.kts 'plugins { id("tech.skot.viewlegacy") }'
put view/src/androidMain/resPreprod/values/v.xml
commit_all
"$script" . --apply >/dev/null
[ "$(cat skot_flavors.properties)" = "environment=preprod,prod" ] || fail "normal : $(cat skot_flavors.properties)"
exists viewmodel/src/main/kotlin/a/A.kt
exists viewmodel/src/main/kotlin/a/N.kt
exists viewmodel/src/preprod/kotlin/a/P.kt
exists view/src/preprod/res/values/v.xml
absent variant

# --- Collision ------------------------------------------------------------------------------------
new_repo
put viewmodel/build.gradle.kts 'plugins { id("tech.skot.viewmodel") }'
put viewmodel/src/commonMain/kotlin/a/A.kt
put viewmodel/src/androidMain/kotlin/a/A.kt
commit_all
if out="$("$script" . --apply 2>&1)"; then fail "collision acceptée"; fi
case "$out" in *"viewmodel/src/main/kotlin/a/A.kt"*) ;; *) fail "collision non signalée : $out" ;; esac
exists viewmodel/src/commonMain/kotlin/a/A.kt
[ -z "$(git status --porcelain)" ] || fail "la collision a modifié l'arbre"

# --- Suffixe inconnu ------------------------------------------------------------------------------
new_repo
put skot/build.gradle.kts 'skSwitchTask("Prod", "prod")'
put viewmodel/build.gradle.kts 'plugins { id("tech.skot.viewmodel") }'
put viewmodel/src/commonMain/kotlinFoo/a/A.kt
commit_all
if out="$("$script" . 2>&1)"; then fail "suffixe inconnu accepté"; fi
case "$out" in *"kotlinFoo"*) ;; *) fail "suffixe inconnu non signalé : $out" ;; esac

echo OK
