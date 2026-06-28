@echo off
setlocal

cd /d "%~dp0"

if not exist "backend\target\viatrial-backend-0.1.1.jar" (
  echo Jar not found. Please run:
  echo   cd backend
  echo   mvn package
  exit /b 1
)

if not exist "data" mkdir "data"

echo Starting ViaTrial...
echo Open http://localhost:8080 after startup.
java -jar "backend\target\viatrial-backend-0.1.1.jar"

endlocal
