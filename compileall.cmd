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

:: Set local variable scope.
setlocal

:: Check args.
if /I "%~1"=="legacy" (
    :: Build in legacy mode.
    echo SCRIPT: Building in legacy mode...
    cmd.exe /C gradlew.bat -Dru.vidtu.ias.legacy=true assemble
    echo SCRIPT: Building in legacy mode exited with code %ERRORLEVEL%.
    goto :end
)
if /I "%~1"=="normal" (
    :: Build in normal mode.
    echo SCRIPT: Building in normal mode...
    cmd.exe /C gradlew.bat assemble
    echo SCRIPT: Building in normal mode exited with code %ERRORLEVEL%.
    goto :end
)
echo ERROR: You must specify the mode of execution.
echo Normal (Beta/Active): compileall.cmd normal
echo Legacy (Beta/Active/Legacy): compileall.cmd legacy
exit /B 2

:end
:: End local variable scope.
endlocal
