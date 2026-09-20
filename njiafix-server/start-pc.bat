@echo off
cd /d "%~dp0"
where node >nul 2>nul || (echo Weka Node.js kwanza: https://nodejs.org & pause & exit /b 1)
if not exist node_modules (call npm install)
where adb >nul 2>nul || echo ONYO: adb haipo kwenye PATH - weka Android Platform Tools ili amri za simu zifanye kazi.
echo Server itasikiliza 127.0.0.1:5555. Kwa simu ya fundi kuunganisha: set HOST=0.0.0.0 kabla ya kuanza.
node server.js
pause
