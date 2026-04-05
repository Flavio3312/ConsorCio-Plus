@echo off
set "MARIADB_DIR=C:\Users\flavi\Desktop\SEMINARIO DE PRACTICA INFORMATICA\TP1\mariadb_temp\mariadb-10.11.7-winx64"

echo ========================================================
echo Apagando Base de Datos Portable (Puerto 3307)
echo ========================================================
echo.

"%MARIADB_DIR%\bin\mysqladmin.exe" -u root -P 3307 shutdown
echo Base de datos detenida exitosamente.
pause
