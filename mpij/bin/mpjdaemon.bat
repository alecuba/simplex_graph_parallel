@echo off
set JAVA_HOME=""%CD%\..\..\..\..\..\App\Java""
set MPJ_HOME=%CD%\\..\\.
SET PATH=%PATH%;%MPJ_HOME%\\bin;%JAVA_HOME%\\bin"
set count=0
for %%i in (%*) do set /A count+=1

if NOT %count% == 1 goto :notValid


if %1 == -boot (
%JAVA_HOME%\bin\java -jar %MPJ_HOME%/lib/daemonmanager.jar -winboot
goto :eof
)

if %1 == -halt (
%JAVA_HOME%\bin\java -jar %MPJ_HOME%/lib/daemonmanager.jar -winhalt 
goto :eof
)

if %1 == -status (
%JAVA_HOME%\bin\java -jar %MPJ_HOME%/lib/daemonmanager.jar -status -hosts localhost
goto :eof
)

:notValid
echo "Usage: mpjdaemon.bat { -boot | -halt | -status }"
goto :eof
