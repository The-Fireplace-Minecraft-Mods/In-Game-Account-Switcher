:: In-Game Account Switcher is a third-party mod for Minecraft Java Edition that
:: allows you to change your logged in account in-game, without restarting it.
::
:: Copyright (C) 2015-2022 The_Fireplace
:: Copyright (C) 2021-2026 VidTu
::
:: This program is free software: you can redistribute it and/or modify
:: it under the terms of the GNU Lesser General Public License as published by
:: the Free Software Foundation, either version 3 of the License, or
:: (at your option) any later version.
::
:: This program is distributed in the hope that it will be useful,
:: but WITHOUT ANY WARRANTY; without even the implied warranty of
:: MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
:: GNU Lesser General Public License for more details.
::
:: You should have received a copy of the GNU Lesser General Public License
:: along with this program.  If not, see <https://www.gnu.org/licenses/>

:: Disable echo.
@echo off

:: Set local variable scope. (enable delayed expansions)
setlocal enabledelayedexpansion

:: Iterate.
echo SCRIPT: Launching all versions...
for /D %%F in (versions\*) do (
    :: Launch.
    echo SCRIPT: Launching '%%~nxF'...
    cmd.exe /c gradlew.bat "-Dru.vidtu.ias.only=%%~nxF" "%%~nxF:runClient"
    echo SCRIPT: Launch for '%%~nxF' exited with code !ERRORLEVEL!.
    if not !ERRORLEVEL!==0 (
        echo SCRIPT: Non-zero exit code. Press any key to continue, terminate ^(CTRL+C^) to cancel.
        pause >nul
    )
)
echo SCRIPT: Done launching all versions.

:: End local variable scope.
endlocal
