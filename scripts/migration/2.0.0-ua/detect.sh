#!/usr/bin/env bash
# Inventaire en lecture seule d'un projet SKot 1.6 avant migration vers 2.0.0-ua.
# Usage : detect.sh <racine du projet>
set -euo pipefail
root="${1:?usage: detect.sh <project-root>}"
cd "$root"
prune=(-name build -prune -o -name .claude -prune -o -name .worktrees -prune -o -name .git -prune -o)
excluded=(--exclude-dir=build --exclude-dir=.claude --exclude-dir=.worktrees --exclude-dir=.git)
modules=(viewcontract modelcontract viewmodel model)

echo "== 1. Déclarations expect et leurs actual"
for m in "${modules[@]}"; do
  [ -d "$m/src" ] || continue
  find "$m/src" "${prune[@]}" -name '*.kt' -print | while IFS= read -r f; do
    grep -nE '^[[:space:]]*([a-z]+[[:space:]]+)*expect[[:space:]]' "$f" | sed "s|^|$f:|" || true
  done
done
echo "-- actual :"
for m in "${modules[@]}"; do
  [ -d "$m/src" ] || continue
  grep -rlE '^[[:space:]]*([a-z]+[[:space:]]+)*actual[[:space:]]' "$m/src" "${excluded[@]}" --include='*.kt' 2>/dev/null || true
done

echo "== 2. Code et ressources Android dans les contrats (à déplacer dans model)"
for m in viewcontract modelcontract; do
  [ -d "$m/src/androidMain" ] || continue
  find "$m/src/androidMain" "${prune[@]}" -type f ! -name AndroidManifest.xml -print
done

echo "== 3. Fichiers utilisant androidApplication (import tech.skot.core.di.androidApplication)"
grep -rlw androidApplication . "${excluded[@]}" --exclude-dir=generated --include='*.kt' 2>/dev/null || true

echo "== 4. DSL Kotlin Multiplatform dans les scripts des modules"
for m in "${modules[@]}"; do
  [ -f "$m/build.gradle.kts" ] || continue
  grep -nE 'kotlin\("multiplatform"\)|^[[:space:]]*kotlin \{|androidMain|commonMain|jvmMain' "$m/build.gradle.kts" | sed "s|^|$m/build.gradle.kts:|" || true
done

echo "== 5. Cibles jvm() hors modules SKot, et qui en dépend"
grep -rlE '^[[:space:]]*jvm\(' . "${excluded[@]}" --include='*.gradle.kts' 2>/dev/null || true
for m in "${modules[@]}"; do
  [ -f "$m/build.gradle.kts" ] || continue
  grep -nE 'projects\.|project\("' "$m/build.gradle.kts" | sed "s|^|$m/build.gradle.kts:|" || true
done

echo "== 6. Fichiers écrits à la main utilisant modelFrameworkModule (hors generated/ et jvmTest/)"
grep -rlw modelFrameworkModule . "${excluded[@]}" --exclude-dir=generated --exclude-dir=jvmTest --include='*.kt' 2>/dev/null || true
