@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\iniciar-enlace-publico.ps1" -Abrir
if errorlevel 1 echo No se pudo verificar el enlace. Revisa el mensaje anterior.
pause
