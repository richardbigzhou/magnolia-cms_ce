@echo off
if ""%1"" == ""start"" goto doStart
if ""%1"" == ""stop"" goto doStop
goto noCommand

:doStart
call startup.bat
goto end

:doStop
call shutdown.bat
goto end

:noCommand
echo Please provide "start" or "stop" as argument.

:end
pause
