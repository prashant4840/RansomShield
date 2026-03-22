#!/usr/bin/env bash
# RansomShield — Build Android APK
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/android-app"
echo "Building Android debug APK..."
./gradlew assembleDebug --no-daemon
echo "APK: android-app/app/build/outputs/apk/debug/app-debug.apk"
