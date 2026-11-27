#!/usr/bin/env bash
# Build a test APK locally, independently of GitHub Actions.
set -euo pipefail

root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
project="$root/Android/src"
sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}}"
java="java"
javac="javac"
if [[ -n "${JAVA_HOME:-}" ]]; then
  java="$JAVA_HOME/bin/java"
  javac="$JAVA_HOME/bin/javac"
fi
fail() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

[[ -f "$project/gradlew" ]] || fail "Android Gradle wrapper not found: $project/gradlew"
command -v "$java" >/dev/null || fail "Install JDK 21 and set JAVA_HOME."
version="$("$java" -version 2>&1)"
[[ "$version" =~ version\ \"21([.\"-]) ]] || fail "JDK 21 is required: $version"
command -v "$javac" >/dev/null || fail "JDK 21 with javac is required. Set JAVA_HOME to the JDK directory."
compiler_version="$("$javac" -version 2>&1)"
[[ "$compiler_version" =~ (^|$'\n')javac\ 21([.\ -]|$) ]] || fail "JDK 21 is required: $compiler_version"
[[ -d "$sdk" ]] || fail "Install Android SDK Command-line Tools and set ANDROID_HOME."
export ANDROID_HOME="$sdk" ANDROID_SDK_ROOT="$sdk"

# Do not fill a server disk with Gradle caches and Android SDK downloads.
for dir in "$project" "$sdk" "${GRADLE_USER_HOME:-$HOME}"; do
  [[ -d "$dir" ]] || continue
  free_kib="$(df -Pk -- "$dir" | awk 'END {print $4}')"
  [[ "$free_kib" -ge 15728640 ]] || fail "Less than 15 GiB free on $dir. Recommended: 30–40 GiB."
done
if [[ -r /proc/meminfo ]]; then
  mem_kib="$(awk '/^MemTotal:/ {print $2}' /proc/meminfo)"
  [[ "$mem_kib" -ge 6291456 ]] || fail "Less than 6 GiB RAM. Recommended: 8–16 GiB."
fi

platform="$sdk/platforms/android-37.0/android.jar"
if [[ ! -f "$platform" ]]; then
  sdkmanager="$sdk/cmdline-tools/latest/bin/sdkmanager"
  [[ -x "$sdkmanager" ]] || fail "Android sdkmanager is missing: $sdkmanager"
  printf '%s\n' 'Installing the SDK platform required by the project. Accept SDK licenses if prompted.'
  "$sdkmanager" --sdk_root="$sdk" 'platforms;android-37.0'
fi
[[ -f "$platform" ]] || fail "Required Android SDK Platform 37.0 not found after installation."

cd "$project"
# The upstream release variant uses debug signing. For local testing only.
bash ./gradlew --no-daemon --max-workers=2 :app:assembleRelease
shopt -s nullglob
apks=(app/build/outputs/apk/release/*.apk)
(( ${#apks[@]} > 0 )) || fail 'Gradle returned success but no APK was found.'
printf '\nBuilt test APK(s):\n'
for apk in "${apks[@]}"; do
  [[ -s "$apk" ]] || fail "Empty APK: $apk"
  sha256sum -- "$apk"
  printf '%s/%s\n' "$project" "$apk"
done
printf '%s\n' 'Test builds use the local debug keystore. Keep it private for compatible updates.'
