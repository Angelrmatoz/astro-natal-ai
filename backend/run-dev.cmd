@echo off
setlocal
cd /d "%~dp0"
set SPRING_PROFILES_ACTIVE=dev
infisical run --env=dev -- cmd /c "mvnw.cmd spring-boot:run"
endlocal
