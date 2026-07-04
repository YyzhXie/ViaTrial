@echo off
setlocal

cd /d "%~dp0"

set "JAR=backend\target\viatrial-backend-0.1.3.jar"

if not exist "%JAR%" (
  echo Jar not found. Building ViaTrial first...

  where npm >nul 2>nul
  if errorlevel 1 (
    echo npm was not found. Please install Node.js 22 or later.
    exit /b 1
  )

  where mvn >nul 2>nul
  if errorlevel 1 (
    echo Maven was not found. Please install Maven 3.9.x.
    exit /b 1
  )

  pushd frontend
  call npm install
  if errorlevel 1 exit /b 1
  call npm run build
  if errorlevel 1 exit /b 1
  popd

  if exist "backend\src\main\resources\static" rmdir /s /q "backend\src\main\resources\static"
  mkdir "backend\src\main\resources\static"
  xcopy "frontend\dist\*" "backend\src\main\resources\static\" /E /I /Y >nul
  if errorlevel 1 exit /b 1

  pushd backend
  call mvn package
  if errorlevel 1 exit /b 1
  popd
)

if not exist "data" mkdir "data"

echo Starting ViaTrial...
echo Open http://localhost:8080 after startup.
java -jar "%JAR%"

endlocal
