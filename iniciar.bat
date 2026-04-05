@echo off
set "JDK_BIN=C:\Users\flavi\Desktop\SEMINARIO DE PRACTICA INFORMATICA\TP1\jdk1.8\java-1.8.0-openjdk-1.8.0.482.b08-1.win.jdk.x86_64\bin"
cd /d "%~dp0"

echo =======================================================
echo Iniciando ConsorCio+ (Sistema de Gestion de Consorcios)
echo Utilizando Java 1.8 almacenado localmente...
echo =======================================================

"%JDK_BIN%\java.exe" -cp "out;lib\mysql-connector-j-8.3.0.jar" com.consorcioplus.Main

if errorlevel 1 (
    echo.
    echo Ocurrio un error al ejecutar la aplicacion. 
    echo Asegurate de haber hecho doble clic primero en 'iniciar_BD.bat'
    echo para levantar la base de datos portable que te instale.
)

pause
