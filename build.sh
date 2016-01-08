#!/bin/bash
GRADLE_PATH="./gradlew"
echo "The_Fireplace's Forge Tools - Build v1.2"

which gradle && GRADLE_PATH="$(which gradle)"

"$GRADLE_PATH" build
echo "****************************"
echo "Building mod completed!"
echo "****************************"
echo "Press any key to continue..."
read -n 1 c