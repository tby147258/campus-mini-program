@REM ----------------------------------------------------------------------------
@REM Maven Wrapper - auto-download and use specified Maven version
@REM Usage: mvnw.cmd spring-boot:run
@REM        mvnw.cmd compile
@REM        mvnw.cmd clean install
@REM ----------------------------------------------------------------------------
@REM Limited for Windows CMD - full wrapper downloads Maven automatically

@if "%DEBUG%"=="" @echo off
@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome
set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute
echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2
goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe
if exist "%JAVA_EXE%" goto execute
echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2
goto fail

:execute
@rem Setup the command line
set CLASSPATH=%APP_HOME%\.mvn\wrapper\maven-wrapper.jar

@rem Download Maven if wrapper jar doesn't exist
if not exist "%CLASSPATH%" (
    echo Downloading Maven Wrapper jar... 1>&2
    @REM Try to download using PowerShell
    powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://maven.aliyun.com/repository/central/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar' -OutFile '%CLASSPATH%'; Write-Host 'Downloaded maven-wrapper.jar'}" 2>&1
    if %ERRORLEVEL% equ 0 (
        echo Maven Wrapper jar downloaded. 1>&2
    ) else (
        echo.
        echo WARNING: Could not download Maven Wrapper jar. 1>&2
        echo You can manually download it from: 1>&2
        echo   https://maven.aliyun.com/repository/central/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar 1>&2
        echo And save it to: %CLASSPATH% 1>&2
        goto execute_with_maven_home
    )
)

@REM Execute Maven using the wrapper jar
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% "-Dmaven.multiModuleProjectDirectory=%APP_HOME%\campus-backend" -classpath "%CLASSPATH%" org.apache.maven.wrapper.MavenWrapperMain %*
goto :EOF

:execute_with_maven_home
@REM Fallback: use MAVEN_HOME if set
if defined MAVEN_HOME (
    "%MAVEN_HOME%\bin\mvn.cmd" %*
) else (
    echo.
    echo ERROR: Neither MAVEN_HOME nor Maven Wrapper jar is available. 1>&2
    echo Install Maven or ensure the wrapper jar is downloaded. 1>&2
    goto fail
)

:fail
@endlocal
exit /b 1