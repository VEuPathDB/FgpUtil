#!/bin/bash
# Usage: ./sync-module-versions.sh [pom_dir]
# Syncs all module parent versions to match the root pom version.

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_POM="$SCRIPT_DIR/pom.xml"

# Extract root version
ROOT_VERSION=$(sed -n 's|.*<version>\(.*\)</version>.*|\1|p' "$ROOT_POM" | head -1)
if [ -z "$ROOT_VERSION" ]; then
  echo "ERROR: Could not determine root pom version" >&2
  exit 1
fi

# Extract root artifactId
ROOT_ARTIFACT=$(sed -n 's|.*<artifactId>\(.*\)</artifactId>.*|\1|p' "$ROOT_POM" | head -1)
if [ -z "$ROOT_ARTIFACT" ]; then
  echo "ERROR: Could not determine root pom artifactId" >&2
  exit 1
fi

echo "Root: $ROOT_ARTIFACT @ $ROOT_VERSION"

# Find all module poms that declare this project as parent
find "$SCRIPT_DIR" -mindepth 2 -maxdepth 2 -name pom.xml | while read -r module_pom; do
  # Only update if this pom references the root artifact as its parent
  if grep -q "<artifactId>$ROOT_ARTIFACT</artifactId>" "$module_pom"; then
    CURRENT=$(sed -n '/<parent>/,/<\/parent>/s|.*<version>\(.*\)</version>.*|\1|p' "$module_pom")
    if [ "$CURRENT" = "$ROOT_VERSION" ]; then
      echo "  $(dirname "$module_pom" | xargs basename): already $ROOT_VERSION"
    else
      sed -i "/<parent>/,/<\/parent>/s|<version>$CURRENT</version>|<version>$ROOT_VERSION</version>|" "$module_pom"
      echo "  $(dirname "$module_pom" | xargs basename): $CURRENT -> $ROOT_VERSION"
    fi
  fi
done
