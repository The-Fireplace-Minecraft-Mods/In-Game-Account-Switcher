@ECHO OFF
TITLE The_Fireplace's Forge Tools - Offline Build v1.1
call gradlew --offline build jar
ECHO ****************************
ECHO Building mod completed!
ECHO ****************************
PAUSE