@echo off
setlocal
cd /d "%~dp0"
if "%PORT%"=="" set PORT=8080
java -version >nul 2>&1
if errorlevel 1 (
  echo Java 21 or newer is required. Install a JDK and try again.
  exit /b 1
)
mvn -version >nul 2>&1
if errorlevel 1 (
  echo Maven 3.9 or newer is required. Install Maven and try again.
  exit /b 1
)
echo Starting VA Hotel. Open http://localhost:%PORT% when startup completes. Stop with Ctrl+C.
mvn spring-boot:run
