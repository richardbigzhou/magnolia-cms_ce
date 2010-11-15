@echo off
rem -- the $JDKPath variable can be replaced in this script by external tools such as an installer
if exist "$JDKPath" set JAVA_HOME=$JDKPath

rem Magnolia needs extra memory
set CATALINA_OPTS=%CATALINA_OPTS% -XX:MaxPermSize=256m -Xms64M -Xmx512M -Djava.awt.headless=true

set CURDIR=%~dp0
if exist "%CURDIR%\magnolia_banner.txt" type %CURDIR%\magnolia_banner.txt
