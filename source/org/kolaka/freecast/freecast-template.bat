@echo off
set JAVA_CMD=java
if not "%JAVA_HOME%" == "" set JAVA_CMD=%JAVA_HOME%\bin\java

%JAVA_CMD% -version > nul
if not ERRORLEVEL 1 goto javaok

echo "Can't find a Java(TM) 2 Runtime Environment."
echo "Use the JAVA_HOME variable"

goto end

:javaok

if not "%FREECAST_HOME%" == "" goto homeok

rem to support an launch from the bin directory
if exist lib set FREECAST_HOME=.
if exist ..\lib set FREECAST_HOME=..

:homeok

set LIBDIR=%FREECAST_HOME%\lib

if exist %LIBDIR% goto libok

echo "Can't find the freecast lib."
echo "Use the FREECAST_HOME variable"

:libok

set LOGDIR=%FREECAST_HOME%\log
if not exist %LOGDIR% md %LOGDIR%
if not exist %LOGDIR% set LOGDIR=.

rem create classpath

if exist %FREECAST_HOME%\etc set CLASSPATH=%FREECAST_HOME%\etc
FOR  %%i IN (%LIBDIR%\*.jar) DO call %FREECAST_HOME%\bin\classpath %%i
FOR  %%i IN (%LIBDIR%\windows\*.jar) DO call %FREECAST_HOME%\bin\classpath %%i

set PATH=%PATH%;%LIBDIR%\windows\x86

%JAVA_CMD% -classpath %CLASSPATH% -Dapp.name=@app.name@ -Dlog.dir=%LOGDIR% -Dlib.dir=%LIBDIR% @app.mainclass@ %*

if ERRORLEVEL 1 pause

:end
