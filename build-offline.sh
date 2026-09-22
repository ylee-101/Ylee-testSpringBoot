#!/usr/bin/env bash
# Reproducible, network-free build entrypoint.
#
# Prerequisite: a JDK 21 installation must already exist on this machine
# (Gradle's own toolchain auto-detection scans ~/.gradle/jdks,
# /Library/Java/JavaVirtualMachines, and JAVA_HOME). This script does not
# download or bundle a JDK. See DEVELOPMENT_ENVIRONMENT_REQUIREMENTS.md.
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

TASK="${1:-build}"

GRADLE_USER_HOME="$PROJECT_ROOT/.gradle-offline-home"
mkdir -p "$GRADLE_USER_HOME"

GRADLE_DIST_NAME="gradle-9.7.1-bin"
GRADLE_DIST_HASH="1w1c7tv4s851m17nbqdsro2tv" # deterministic hash of the distributionUrl, see gradle/wrapper/gradle-wrapper.properties
DIST_DIR="$GRADLE_USER_HOME/wrapper/dists/$GRADLE_DIST_NAME/$GRADLE_DIST_HASH"
SPLIT_DIR="$PROJECT_ROOT/offline-tools/gradle-dist"
RECONSTRUCTED_ZIP="$GRADLE_USER_HOME/.reconstructed-$GRADLE_DIST_NAME.zip"

# --- 1. Make the Gradle distribution available locally, without a network request ---
if [ ! -d "$DIST_DIR/gradle-9.7.1" ]; then
	echo "Reassembling $GRADLE_DIST_NAME.zip from split parts..."
	cat "$SPLIT_DIR"/${GRADLE_DIST_NAME}.zip.part-* > "$RECONSTRUCTED_ZIP"

	EXPECTED_SHA256="$(cat "$SPLIT_DIR/${GRADLE_DIST_NAME}.zip.sha256")"
	ACTUAL_SHA256="$(shasum -a 256 "$RECONSTRUCTED_ZIP" | awk '{print $1}')"
	if [ "$EXPECTED_SHA256" != "$ACTUAL_SHA256" ]; then
		echo "ERROR: reconstructed $GRADLE_DIST_NAME.zip checksum mismatch." >&2
		echo "  expected: $EXPECTED_SHA256" >&2
		echo "  actual:   $ACTUAL_SHA256" >&2
		rm -f "$RECONSTRUCTED_ZIP"
		exit 1
	fi

	mkdir -p "$DIST_DIR"
	unzip -q "$RECONSTRUCTED_ZIP" -d "$DIST_DIR"
	touch "$DIST_DIR/${GRADLE_DIST_NAME}.zip.ok" "$DIST_DIR/${GRADLE_DIST_NAME}.zip.lck"
	rm -f "$RECONSTRUCTED_ZIP"
fi

# --- 2. Validate a JDK 21 is present (required, not bundled) ---
JDK21_HOME=""
for candidate in \
	"${JAVA_HOME:-}" \
	"$GRADLE_USER_HOME/jdks"/*21*/*/Contents/Home \
	"$HOME/.gradle/jdks"/*21*/*/Contents/Home \
	/Library/Java/JavaVirtualMachines/*21*/Contents/Home \
	/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
do
	if [ -n "$candidate" ] && [ -x "$candidate/bin/java" ]; then
		VERSION="$("$candidate/bin/java" -version 2>&1 | head -1)"
		if [[ "$VERSION" == *"\"21."* ]]; then
			JDK21_HOME="$candidate"
			break
		fi
	fi
done

if [ -z "$JDK21_HOME" ]; then
	echo "ERROR: no JDK 21 installation found." >&2
	echo "This project requires a preinstalled JDK 21 (Eclipse Temurin recommended)." >&2
	echo "See DEVELOPMENT_ENVIRONMENT_REQUIREMENTS.md for installation instructions." >&2
	exit 1
fi

echo "Using JDK 21 at: $JDK21_HOME"
echo "Using project-local GRADLE_USER_HOME: $GRADLE_USER_HOME"
echo "Running offline task: $TASK"

JAVA_HOME="$JDK21_HOME" "$PROJECT_ROOT/gradlew" "$TASK" \
	--offline \
	-PofflineMavenOnly=true \
	--gradle-user-home "$GRADLE_USER_HOME"
