#!/usr/bin/env bash
# Compila, ejecuta las pruebas y corre la simulación de la taberna (Linux / macOS / Git Bash).
set -e
cd "$(dirname "$0")"

rm -rf out
mkdir out
javac --release 17 -encoding UTF-8 -d out $(find src -name "*.java")

echo ">>> Pruebas del Event Bus"
java -Dfile.encoding=UTF-8 -cp out taberna.PruebasDelBusDeEventos

echo ">>> Simulación de la taberna"
java -Dfile.encoding=UTF-8 -cp out taberna.Main
