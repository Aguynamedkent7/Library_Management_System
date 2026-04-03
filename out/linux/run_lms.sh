#!/bin/bash

# Configuration
# Replace the database URL with your own settings
export LMS_DB_URL="jdbc:postgresql://localhost:5432/lms_db?user=postgres&password=Cj030304$"

# Fix for Java Swing apps on Wayland/Tiling Window Managers (like Niri)
export _JAVA_AWT_WM_NONREPARENTING=1
export NO_AT_BRIDGE=1
export GDK_BACKEND=x11

# Improve font rendering and UI scaling
export _JAVA_OPTIONS="-Dawt.useSystemAAFontSettings=on -Dswing.aatext=true -Dsun.java2d.uiScale=1"

# Navigate to the app directory
cd "$(dirname "$0")/../../LMS_APP"

# Check if JAR exists, if not build it
JAR_PATH="target/qr-code-reader-1.0-SNAPSHOT-jar-with-dependencies.jar"
if [ ! -f "$JAR_PATH" ]; then
    echo "JAR not found. Attempting to build..."
    if command -v mvn &> /dev/null; then
        mvn clean package -DskipTests
    else
        echo "Error: Maven (mvn) is not installed. Please install it to build the project."
        exit 1
    fi
fi

# Run the application
echo "Starting Library Management System..."
java -jar "$JAR_PATH"
