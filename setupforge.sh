#!/bin/bash
GRADLE_PATH="./gradlew"
echo "The_Fireplace's Forge Tools - Setup v1.5"

which gradle && GRADLE_PATH="$(which gradle)"

"$GRADLE_PATH" setupDecompWorkspace
"$GRADLE_PATH" idea
echo "****************************"
echo "Forge idea workspaces setup complete!"
echo "****************************"
echo "Press any key to continue..."
read -n 1 c