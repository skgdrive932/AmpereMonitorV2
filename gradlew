#!/bin/sh
set -e
GRADLE_VERSION="8.11.1"
GRADLE_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
DIST_DIR="$GRADLE_HOME/ampere-wrapper/gradle-$GRADLE_VERSION"
GRADLE_BIN="$DIST_DIR/bin/gradle"
if [ ! -x "$GRADLE_BIN" ]; then
  TMP_DIR="$GRADLE_HOME/ampere-wrapper/tmp"
  ZIP_FILE="$TMP_DIR/gradle-$GRADLE_VERSION-bin.zip"
  mkdir -p "$TMP_DIR"
  echo "Downloading Gradle $GRADLE_VERSION..."
  if command -v curl >/dev/null 2>&1; then
    curl -fL --retry 3 "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ZIP_FILE"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP_FILE" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  else
    echo "Error: curl or wget is required." >&2; exit 1
  fi
  rm -rf "$DIST_DIR"
  mkdir -p "$GRADLE_HOME/ampere-wrapper"
  unzip -q "$ZIP_FILE" -d "$GRADLE_HOME/ampere-wrapper"
  rm -f "$ZIP_FILE"
fi
exec "$GRADLE_BIN" "$@"
