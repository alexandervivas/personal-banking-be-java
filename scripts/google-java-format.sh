#!/usr/bin/env bash
set -euo pipefail

# Version pin (override with env GJF_VERSION if needed)
GJF_VERSION="${GJF_VERSION:-1.22.0}"
CACHE_DIR="${CACHE_DIR:-.git-hooks-cache}"
JAR_NAME="google-java-format-${GJF_VERSION}-all-deps.jar"
JAR_PATH="${CACHE_DIR}/${JAR_NAME}"
MAVEN_URL="https://repo1.maven.org/maven2/com/google/googlejavaformat/google-java-format/${GJF_VERSION}/${JAR_NAME}"

mkdir -p "${CACHE_DIR}"

if [[ ! -f "${JAR_PATH}" ]]; then
echo "Downloading google-java-format ${GJF_VERSION}…" >&2
curl -fsSL "${MAVEN_URL}" -o "${JAR_PATH}"
fi

# Pass-through: flags first (e.g., --aosp --replace), then the file list from pre-commit
exec java -jar "${JAR_PATH}" "$@"
