#!/bin/bash
GRADLE_PATH="./gradlew"
echo "The_Fireplace's Forge Tools - Offline Build v1.1"

which gradle && GRADLE_PATH="$(which gradle)"

"$GRADLE_PATH" --offline build jar
echo "****************************"
echo "Building mod completed!"
echo "****************************"
echo "Press any key to continue..."
read -n 1 c