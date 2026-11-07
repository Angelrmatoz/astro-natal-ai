@echo off
setlocal
cd /d "%~dp0"
infisical run --env=dev -- cmd /c "mvnw.cmd spring-boot:run"
endlocal
