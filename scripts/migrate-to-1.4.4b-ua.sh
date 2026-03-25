#!/usr/bin/env bash
# migrate-to-1.4.4b-ua.sh
# Migration automatique d'un projet Skot vers la version 1.4.4b-ua (AGP 9.0)
#
# Usage :
#   ./scripts/migrate-to-1.4.4b-ua.sh [chemin/vers/projet]
#   (sans argument : répertoire courant)

set -euo pipefail

PROJECT_DIR="${1:-.}"
PROJECT_DIR="$(cd "$PROJECT_DIR" && pwd)"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

info()    { echo -e "${BLUE}[INFO]${NC} $1"; }
ok()      { echo -e "${GREEN}[OK]${NC}   $1"; }
warn()    { echo -e "${YELLOW}[WARN]${NC} $1"; }
error()   { echo -e "${RED}[ERROR]${NC} $1"; }
section() { echo -e "\n${BLUE}━━━ $1 ━━━${NC}"; }

CHANGED=0
changed() { CHANGED=$((CHANGED + 1)); ok "$1"; }

# ─── Utilitaires ────────────────────────────────────────────────────────────

# Remplace une ligne dans un fichier (macOS + Linux compatible)
replace_in_file() {
    local file="$1" pattern="$2" replacement="$3"
    if grep -qE "$pattern" "$file" 2>/dev/null; then
        sed -i.bak -E "s|$pattern|$replacement|g" "$file"
        rm -f "${file}.bak"
        return 0
    fi
    return 1
}

# Supprime une ligne contenant un pattern
delete_line() {
    local file="$1" pattern="$2"
    if grep -qF "$pattern" "$file" 2>/dev/null; then
        sed -i.bak "/$pattern/d" "$file"
        rm -f "${file}.bak"
        return 0
    fi
    return 1
}

# ─── 1. Gradle wrapper ──────────────────────────────────────────────────────

section "1. Gradle wrapper"

WRAPPER="$PROJECT_DIR/gradle/wrapper/gradle-wrapper.properties"
if [[ -f "$WRAPPER" ]]; then
    CURRENT_GRADLE=$(grep "distributionUrl" "$WRAPPER" | grep -oE "gradle-[0-9]+\.[0-9]+(\.[0-9]+)?")
    TARGET="gradle-9.3.1"
    if [[ "$CURRENT_GRADLE" != "$TARGET" ]]; then
        replace_in_file "$WRAPPER" \
            "distributionUrl=.*" \
            "distributionUrl=https\\\\://services.gradle.org/distributions/gradle-9.3.1-bin.zip"
        changed "gradle-wrapper.properties : mise à jour vers Gradle 9.3.1 (était $CURRENT_GRADLE)"
    else
        info "gradle-wrapper.properties : déjà sur Gradle 9.3.1"
    fi
else
    warn "gradle-wrapper.properties introuvable dans $PROJECT_DIR/gradle/wrapper/"
fi

# ─── 2. gradle.properties ───────────────────────────────────────────────────

section "2. gradle.properties"

GRADLE_PROPS="$PROJECT_DIR/gradle.properties"
if [[ -f "$GRADLE_PROPS" ]]; then
    REMOVED_PROPS=(
        "android.enableLegacyVariantApi"
        "android.r8.integratedResourceShrinking"
        "android.enableNewResourceShrinker.preciseShrinking"
        "android.builtInKotlin=false"
        "android.newDsl=false"
    )
    for prop in "${REMOVED_PROPS[@]}"; do
        if delete_line "$GRADLE_PROPS" "$prop"; then
            changed "gradle.properties : supprimé '$prop' (incompatible AGP 9.0)"
        fi
    done
    ok "gradle.properties vérifié"
else
    warn "gradle.properties introuvable"
fi

# ─── 3. Version du plugin Skot dans build.gradle.kts racine ────────────────

section "3. Version du plugin Skot"

ROOT_BUILD="$PROJECT_DIR/build.gradle.kts"
if [[ ! -f "$ROOT_BUILD" ]]; then
    ROOT_BUILD="$PROJECT_DIR/build.gradle"
fi

if [[ -f "$ROOT_BUILD" ]]; then
    if grep -q "useradgents.skot:plugin" "$ROOT_BUILD"; then
        CURRENT_SKOT=$(grep -oE "skot:plugin:[0-9a-zA-Z._-]+" "$ROOT_BUILD" | head -1 | cut -d: -f3)
        if [[ "$CURRENT_SKOT" != "1.4.4b-ua" ]]; then
            replace_in_file "$ROOT_BUILD" \
                "(useradgents\.skot:plugin:)[0-9a-zA-Z._-]+" \
                "\11.4.4b-ua"
            changed "build.gradle.kts : version plugin Skot → 1.4.4b-ua (était $CURRENT_SKOT)"
        else
            info "build.gradle.kts : déjà sur la version 1.4.4b-ua"
        fi
    fi
fi

# Version catalog (libs.versions.toml)
VERSION_CATALOG="$PROJECT_DIR/gradle/libs.versions.toml"
if [[ -f "$VERSION_CATALOG" ]]; then
    if grep -q "^skot" "$VERSION_CATALOG"; then
        CURRENT_SKOT=$(grep -E "^skot\s*=" "$VERSION_CATALOG" | grep -oE '"[^"]+"' | tr -d '"')
        if [[ "$CURRENT_SKOT" != "1.4.4b-ua" ]]; then
            replace_in_file "$VERSION_CATALOG" \
                '^(skot\s*=\s*")[^"]+(")', \
                '\11.4.4b-ua\2'
            changed "libs.versions.toml : version skot → 1.4.4b-ua (était $CURRENT_SKOT)"
        else
            info "libs.versions.toml : déjà sur la version 1.4.4b-ua"
        fi
    fi
fi

# ─── 4. Modules KMP : ajout de compileSdk/minSdk ───────────────────────────

section "4. Modules KMP (compileSdk / minSdk)"

KMP_PLUGINS=("tech.skot.viewcontract" "tech.skot.modelcontract" "tech.skot.viewmodel" "tech.skot.model")

find "$PROJECT_DIR" -name "build.gradle.kts" -not -path "*/build/*" -not -path "*/.gradle/*" | while read -r f; do
    # Est-ce un module KMP Skot ?
    IS_KMP=false
    for plugin in "${KMP_PLUGINS[@]}"; do
        if grep -q "\"$plugin\"" "$f" 2>/dev/null; then
            IS_KMP=true
            break
        fi
    done
    [[ "$IS_KMP" == false ]] && continue

    MODULE=$(dirname "$f" | xargs basename)

    # Vérifie si compileSdk est déjà présent dans le bloc android { }
    if grep -q "compileSdk" "$f"; then
        info "$MODULE/build.gradle.kts : compileSdk déjà présent"
        continue
    fi

    # Détecte la ligne namespace = "..." dans le bloc android { }
    if ! grep -q 'namespace\s*=' "$f"; then
        warn "$MODULE/build.gradle.kts : pas de namespace trouvé, ajout manuel nécessaire"
        continue
    fi

    # Insère compileSdk et minSdk après la ligne namespace
    sed -i.bak '/namespace\s*=/{
        /compileSdk/!{
            p
            s/.*/\t\t\tcompileSdk = 36/
            p
            s/.*/\t\t\tminSdk = 23/
        }
    }' "$f"
    # Supprime les doublons éventuels
    sed -i.bak '/^$/N;/^\n$/d' "$f"
    rm -f "${f}.bak"

    echo -e "${GREEN}[OK]${NC}   $MODULE/build.gradle.kts : ajout compileSdk=36 / minSdk=23"
    CHANGED=$((CHANGED + 1))
done

# ─── 5. Supprimer androidTarget { publishLibraryVariants(...) } ─────────────

section "5. Suppression androidTarget { publishLibraryVariants(...) }"

find "$PROJECT_DIR" -name "build.gradle.kts" -not -path "*/build/*" -not -path "*/.gradle/*" | while read -r f; do
    if grep -q "publishLibraryVariants" "$f" 2>/dev/null; then
        MODULE=$(dirname "$f" | xargs basename)
        # Supprime le bloc androidTarget { publishLibraryVariants("release") } (sur 1 à 3 lignes)
        perl -i -0pe 's/\n?\s*androidTarget\s*\{[^}]*publishLibraryVariants[^}]*\}//g' "$f"
        echo -e "${GREEN}[OK]${NC}   $MODULE/build.gradle.kts : supprimé androidTarget { publishLibraryVariants(...) }"
        CHANGED=$((CHANGED + 1))
    fi
done

# ─── 6. Supprimer org.jetbrains.kotlin.android dans androidApp ─────────────

section "6. Suppression kotlin(\"android\") / org.jetbrains.kotlin.android"

find "$PROJECT_DIR" -name "build.gradle.kts" -not -path "*/build/*" -not -path "*/.gradle/*" | while read -r f; do
    MODULE=$(dirname "$f" | xargs basename)
    FILE_CHANGED=false

    # Forme Kotlin DSL : kotlin("android") avec ou sans version / apply false
    if grep -qE 'kotlin\("android"\)' "$f" 2>/dev/null; then
        sed -i.bak -E '/kotlin\("android"\)/d' "$f"
        rm -f "${f}.bak"
        FILE_CHANGED=true
        echo -e "${GREEN}[OK]${NC}   $MODULE/build.gradle.kts : supprimé kotlin(\"android\")"
        CHANGED=$((CHANGED + 1))
    fi

    # Forme id() explicite : id("org.jetbrains.kotlin.android")
    if grep -qE 'id\("org\.jetbrains\.kotlin\.android"\)' "$f" 2>/dev/null; then
        sed -i.bak -E '/id\("org\.jetbrains\.kotlin\.android"\)/d' "$f"
        rm -f "${f}.bak"
        FILE_CHANGED=true
        echo -e "${GREEN}[OK]${NC}   $MODULE/build.gradle.kts : supprimé id(\"org.jetbrains.kotlin.android\")"
        CHANGED=$((CHANGED + 1))
    fi

    # Forme alias() dans version catalog : alias(libs.plugins.kotlin.android)
    if grep -qE 'alias\(.*kotlin.*android.*\)' "$f" 2>/dev/null; then
        warn "$MODULE/build.gradle.kts : alias kotlin.android trouvé — suppression manuelle recommandée"
        warn "  → Supprimer aussi l'entrée dans libs.versions.toml si plus utilisée ailleurs"
    fi
done

# ─── Résumé ──────────────────────────────────────────────────────────────────

section "Résumé"

if [[ $CHANGED -eq 0 ]]; then
    info "Aucune modification effectuée — le projet est peut-être déjà à jour."
else
    echo -e "${GREEN}$CHANGED modification(s) appliquée(s).${NC}"
fi

echo ""
echo "Étapes manuelles restantes :"
echo "  1. Vérifier compileSdk/minSdk ajoutés dans les modules KMP (valeurs par défaut : 36/23)"
echo "  2. Lancer : ./gradlew :androidApp:assembleDebug"
echo "  3. Vérifier qu'il n'y a pas de 'compileDebugKotlin NO-SOURCE' dans le log"
echo ""
echo "Guide complet : docs/migration/migration-1.4.4b-ua.md"
