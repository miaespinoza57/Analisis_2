@echo off
REM Compila, ejecuta las pruebas y corre la simulacion de la taberna (Windows).
chcp 65001 > nul
cd /d "%~dp0"

if exist out rmdir /s /q out
mkdir out

dir /s /b src\*.java > fuentes.txt
javac --release 17 -encoding UTF-8 -d out @fuentes.txt
if errorlevel 1 (
  del fuentes.txt
  exit /b 1
)
del fuentes.txt

echo ^>^>^> Pruebas del Event Bus
java -Dfile.encoding=UTF-8 -cp out taberna.PruebasDelBusDeEventos

echo ^>^>^> Simulacion de la taberna
java -Dfile.encoding=UTF-8 -cp out taberna.Main
