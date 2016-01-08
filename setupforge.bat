@ECHO OFF
TITLE The_Fireplace's Forge Tools - Setup v1.5
call gradlew setupDecompWorkspace
call gradlew idea
ECHO ****************************
ECHO Forge idea workspace setup complete!
ECHO ****************************
PAUSE