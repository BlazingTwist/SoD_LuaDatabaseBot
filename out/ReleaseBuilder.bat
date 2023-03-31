@echo off
set targetpath=%1
if not [%1] == [] GOTO PathReady

@rem path not ready
set pathToBat=%~dp0
set /p version="No directory provided, input the version number here: "
set targetpath=%pathToBat%%version%
mkdir %targetpath%

:PathReady
xcopy artifacts\SoD_LuaDatabaseBot "%targetpath%" /y /s /e

start /WAIT D:\Tools\WinRAR\Rar.exe m -r -ep1 "SoD_LuaDatabaseBot_%version%_java.rar" "%targetpath%"

RD "%targetpath%" /s /q