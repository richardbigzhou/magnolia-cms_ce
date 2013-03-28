@echo off
if ""%1"" == ""start"" goto doStart
if ""%1"" == ""stop"" goto doStop
goto noCommand

:doStart
if not exist .installed if not exist ..\webapps\magnoliaPublic\WEB-INF goto doInstall
call startup.bat
goto end

:doStop
call shutdown.bat
goto end

:doInstall
echo First run -> create magnoliaPublic webapp from magnoliaAuthor webapp.
if exist ..\webapps\magnoliaPublic rmdir ..\webapps\magnoliaPublic /s /q
xcopy ..\webapps\magnoliaAuthor ..\webapps\magnoliaPublic /e /i /h
echo This file indicates that the public webapp was created. The file is created during first run. > .installed
goto doStart

:noCommand
echo Please provide "start" or "stop" as argument.

:end
pause
