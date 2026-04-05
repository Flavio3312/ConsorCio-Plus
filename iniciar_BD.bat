@echo off
set "MARIADB_DIR=C:\Users\flavi\Desktop\SEMINARIO DE PRACTICA INFORMATICA\TP1\mariadb_temp\mariadb-10.11.7-winx64"

echo ========================================================
echo Iniciando Base de Datos Portable (MariaDB/MySQL)
echo Puerto: 3307
echo ========================================================
echo.

start /b "" "%MARIADB_DIR%\bin\mysqld.exe" --port=3307 --skip-grant-tables --console

echo Base de datos iniciada en segundo plano.
echo Ya podes minimizar esta ventana y abrir iniciar.bat!
echo.
pause
