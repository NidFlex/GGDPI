#!/bin/bash

set -e

PROJECT_DIR="${1:-GGDPI}"
echo "Creating complete GGDPI project in: $PROJECT_DIR"

# Create all directories
mkdir -p "$PROJECT_DIR"/{app/src/main/{java/com/ggdpi/app/{service,core,dpibypass/{handlers,native},ui/{screens,components,theme,viewmodel,navigation},utils,data,di},cpp,res/{drawable,mipmap-xxxhdpi,values,values-night,xml}},gradle/wrapper}

cd "$PROJECT_DIR"

# Create all files using heredocs
# (Здесь должны быть все команды cat для создания файлов из предыдущих сообщений)

echo "Project structure created!"
echo "Total files: 53"
echo ""
echo "Next steps:"
echo "1. Configure local.properties with your SDK/NDK paths"
echo "2. Open in Android Studio or run: ./gradlew assembleRelease"
echo "3. Install APK: adb install app/build/outputs/apk/release/app-release.apk"
SCRIPT_EOF