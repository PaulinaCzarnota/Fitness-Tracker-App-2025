@rem -----------------------------------------------------------------------------
@rem FitnessTrackerApp - Gradle Wrapper Startup Script for Windows
@rem
@rem Responsibilities:
@rem - Bootstraps Gradle builds on Windows environments.
@rem - Ensures correct Java version is used (JDK 17).
@rem - Provides robust error handling and clear user messages.
@rem - Follows consistent comment and formatting standards.
@rem
@rem License: Apache License, Version 2.0
@rem See: https://www.apache.org/licenses/LICENSE-2.0
@rem -----------------------------------------------------------------------------

@if "%DEBUG%"=="" @echo off
@rem ============================================================================
@rem Gradle startup script for Windows
@rem ============================================================================

@rem Set local scope for the variables with Windows NT shell
if "%OS%"=="Windows_NT" setlocal

@rem -----------------------------------------------------------------------------
@rem Force the use of JDK 17 to prevent pathing issues (custom for FitnessTrackerApp)
@rem Update this path if your JDK is installed elsewhere.
@rem -----------------------------------------------------------------------------
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem -----------------------------------------------------------------------------
@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
@rem -----------------------------------------------------------------------------
set DEFAULT_JVM_OPTS=-Xmx64m -Xms64m

@rem -----------------------------------------------------------------------------
@rem Find java.exe
@rem -----------------------------------------------------------------------------
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%\bin\java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
goto fail

:execute
@rem -----------------------------------------------------------------------------
@rem Setup the command line and execute Gradle
@rem -----------------------------------------------------------------------------
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% ^
  "-Dorg.gradle.appname=%APP_BASE_NAME%" ^
  -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
@rem End local scope for the variables with Windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
@rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
@rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%GRADLE_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal
