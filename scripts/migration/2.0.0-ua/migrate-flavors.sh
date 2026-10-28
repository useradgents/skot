#!/usr/bin/env bash
# Passe un projet SKot 2.0.0 de l'arborescence historique (commonMain/androidMain, variantes
# skot_variants suffixées) aux product flavors et à l'arborescence standard (src/main, src/<flavor>).
# Usage : migrate-flavors.sh <racine du projet> [--apply]
# Sans --apply, affiche ce qui serait fait. Avec --apply, l'arbre doit être propre ; rien n'est commité.
set -euo pipefail
root="${1:?usage: migrate-flavors.sh <project-root> [--apply]}"
apply=false
[ "${2:-}" = "--apply" ] && apply=true
cd "$root"
git rev-parse --is-inside-work-tree >/dev/null

work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT
: > "$work/envs"; : > "$work/brands"; : > "$work/plan"; : > "$work/dirs"; : > "$work/errors"
excluded=(--exclude-dir=build --exclude-dir=.gradle --exclude-dir=.git --exclude-dir=.worktrees --exclude-dir=.claude --exclude-dir=generated)

lower_first() { awk '{ print tolower(substr($0, 1, 1)) substr($0, 2) }'; }
upper_first() { awk '{ print toupper(substr($0, 1, 1)) substr($0, 2) }'; }

# 1. Flavors : skSwitchTask("Nom", "environnement", "marque"…), un appel par ligne
grep -rhoE 'skSwitchTask\([^)]*\)' . "${excluded[@]}" --include='*.gradle.kts' 2>/dev/null | while IFS= read -r call; do
  args="$(printf '%s\n' "$call" | grep -oE '"[^"]*"' | tr -d '"')"
  printf '%s\n' "$args" | sed -n 2p | lower_first >> "$work/envs"
  printf '%s\n' "$args" | sed -n '3,$p' | grep -vx 'normal' | lower_first >> "$work/brands" || true
done || true
envs="$(awk 'NF && !seen[$0]++' "$work/envs" | paste -sd, -)"
brands="$(awk 'NF && !seen[$0]++' "$work/brands" | paste -sd, -)"

{ echo main
  printf '%s\n' "$brands" | tr ',' '\n'
  printf '%s\n' "$envs" | tr ',' '\n'
  for b in $(printf '%s' "$brands" | tr ',' ' '); do
    for e in $(printf '%s' "$envs" | tr ',' ' '); do echo "$b$(printf '%s\n' "$e" | upper_first)"; done
  done
} | awk 'NF' > "$work/sourcesets"

# Suffixe historique (SezaneProd, NormalPreprod, Normal…) → source set (sezaneProd, preprod, main), ou vide
source_set() {
  local s="${1#Normal}"
  [ -z "$s" ] && { echo main; return; }
  s="$(printf '%s\n' "$s" | lower_first)"
  if grep -qx "$s" "$work/sourcesets"; then echo "$s"; fi
}

# Planifie le déplacement de chaque fichier de $1 vers le dossier $2
plan_dir() {
  echo "$1 -> $2" >> "$work/dirs"
  find "$1" -type f ! -name .DS_Store | while IFS= read -r f; do
    printf '%s\t%s\n' "$f" "$2/${f#"$1"/}" >> "$work/plan"
  done
}

# 2. Modules SKot à l'ancienne arborescence
modules="$(find . -mindepth 2 -maxdepth 4 -name build.gradle.kts -not -path '*/build/*' -not -path './buildSrc/*' -not -path './.worktrees/*' -exec dirname {} \; | sed 's|^\./||' | sort)"
need_variant=false
for m in $modules; do
  [ -d "$m/src" ] || continue
  contract=false
  grep -qE 'tech\.skot\.(viewcontract|modelcontract)' "$m/build.gradle.kts" && contract=true
  [ -d "$m/src/jvmMain" ] && echo "$m/src/jvmMain : code JVM, à traiter avant (guide de migration 2.0.0-ua)" >> "$work/errors"
  for container in commonMain androidMain; do
    [ -d "$m/src/$container" ] || continue
    for child in "$m/src/$container"/* "$m/src/$container"/.[!.]*; do
      [ -e "$child" ] || continue
      name="$(basename "$child")"
      [ "$name" = .DS_Store ] && continue
      if [ -f "$child" ]; then
        printf '%s\t%s\n' "$child" "$m/src/main/$name" >> "$work/plan"
        continue
      fi
      case "$name" in
        kotlin|res|res_referenced|assets) plan_dir "$child" "$m/src/main/$name" ;;
        kotlin?*)
          ss="$(source_set "${name#kotlin}")"
          if [ -z "$ss" ]; then echo "$child : suffixe inconnu" >> "$work/errors"
          elif $contract && [ "$ss" != main ]; then plan_dir "$child" "variant/src/$ss/kotlin"; need_variant=true
          else plan_dir "$child" "$m/src/$ss/kotlin"; fi ;;
        res?*_referenced)
          suffix="${name#res}"; ss="$(source_set "${suffix%_referenced}")"
          if [ -z "$ss" ]; then echo "$child : suffixe inconnu" >> "$work/errors"; else plan_dir "$child" "$m/src/$ss/res_referenced"; fi ;;
        res?*)
          ss="$(source_set "${name#res}")"
          if [ -z "$ss" ]; then echo "$child : suffixe inconnu" >> "$work/errors"; else plan_dir "$child" "$m/src/$ss/res"; fi ;;
        *) plan_dir "$child" "$m/src/main/$name" ;;
      esac
    done
  done
  for container in jvmTest commonTest; do
    [ -d "$m/src/$container" ] || continue
    for child in "$m/src/$container"/*; do
      [ -d "$child" ] && plan_dir "$child" "$m/src/test/$(basename "$child")"
    done
  done
done

# 3. Collisions : deux sources vers une destination, ou destination déjà présente
cut -f2 "$work/plan" | sort | uniq -d | sed 's/$/ : plusieurs sources/' >> "$work/errors"
cut -f2 "$work/plan" | sort -u | while IFS= read -r dst; do
  [ -e "$dst" ] && echo "$dst : existe déjà" >> "$work/errors"
done || true

echo "== Flavors"
[ -n "$brands" ] && echo "brand=$brands"
echo "environment=${envs:-<aucun skSwitchTask trouvé : pas de skot_flavors.properties>}"
echo "== Déplacements"
cat "$work/dirs"
echo "== Suppressions"
[ -f skot_variants.properties ] && echo "skot_variants.properties"
for m in $modules; do
  for g in "$m"/generated "$m"/generated[A-Z]*; do [ -d "$g" ] && echo "$g"; done
done
$need_variant && echo "== Module :variant créé (variant/build.gradle.kts, include dans settings.gradle.kts)"
echo "== À migrer à la main (guide, section Flavors)"
find buildSrc/src/main -maxdepth 1 -type d -name 'kotlin?*' 2>/dev/null || true
grep -rnE 'skSwitchTask|skBuildSrcVariants|skReadVariants|skVariantsCombinaison|reference(Icons|Fonts|Colors)ByVariant' . "${excluded[@]}" --include='*.kts' --include='*.kt' 2>/dev/null || true
grep -rnE 'skot:[A-Z][A-Za-z]*' . "${excluded[@]}" --include='*.yml' --include='*.yaml' --include='*.sh' --include='Fastfile' --include='Jenkinsfile' 2>/dev/null || true

if [ -s "$work/errors" ]; then
  echo "== Arrêt : à corriger avant toute migration" >&2
  cat "$work/errors" >&2
  exit 1
fi
$apply || { echo "== Simulation : relancez avec --apply"; exit 0; }

if [ -n "$(git status --porcelain -- .)" ]; then
  echo "L'arbre de travail doit être propre (git status) avant --apply." >&2
  exit 1
fi

# 4. Application
if [ -n "$envs" ]; then
  { [ -n "$brands" ] && echo "brand=$brands"; echo "environment=$envs"; } > skot_flavors.properties
fi
while IFS="$(printf '\t')" read -r src dst; do
  mkdir -p "$(dirname "$dst")"
  if git ls-files --error-unmatch "$src" >/dev/null 2>&1; then git mv "$src" "$dst"; else mv "$src" "$dst"; fi
done < "$work/plan"
for m in $modules; do
  for container in commonMain androidMain jvmTest commonTest; do
    [ -d "$m/src/$container" ] || continue
    find "$m/src/$container" -name .DS_Store -delete
    find "$m/src/$container" -depth -type d -empty -delete
  done
  for g in "$m"/generated "$m"/generated[A-Z]*; do
    [ -d "$g" ] || continue
    git rm -r -q --cached --ignore-unmatch "$g"
    rm -rf "$g"
  done
done
if [ -f skot_variants.properties ]; then
  git rm -q --cached --ignore-unmatch skot_variants.properties
  rm -f skot_variants.properties
fi
if $need_variant && [ ! -f variant/build.gradle.kts ]; then
  package="$(grep -hoE 'packageName *= *"[^"]+"' skot/build.gradle.kts 2>/dev/null | head -1 | sed -E 's/.*"([^"]+)"/\1/')"
  printf 'plugins {\n    id("tech.skot.variant")\n}\n\nandroid {\n    namespace = "%s.variant"\n}\n' "${package:-TODO.package}" > variant/build.gradle.kts
  mkdir -p variant/src/main
  printf '<manifest />\n' > variant/src/main/AndroidManifest.xml
  grep -q 'include(":variant")' settings.gradle.kts || printf '\ninclude(":variant")\n' >> settings.gradle.kts
fi
echo "== Fait. Vérifiez (git status), puis : git add -A && git commit -m \"CHORE - Move the sources to the standard flavor layout (moves only)\""
