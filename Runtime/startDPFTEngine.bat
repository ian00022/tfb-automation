#Script to start DPFT Engine

set PATH=%PATH%;C:\Program Files\Java\jdk1.8.0_144\bin
set EXEC_HOME=D:\IBM\DPFT\Runtime-Zack
set RERUN_SCRIPT_PATH=%EXEC_HOME%\resources\scripts\automation_lite.properties

echo START!!!!

:START_PROG
start java -XX:-UseGCOverheadLimit -Xmx20G -Xms20G -Xmn5G -XX:PermSize=128m -XX:MaxPermSize=256m -XX:+UseG1GC -XX:ParallelGCThreads=20 -XX:MaxGCPauseMillis=200 -XX:ConcGCThreads=5 -XX:InitiatingHeapOccupancyPercent=70 -jar -Dlog4j.configuration=file:%EXEC_HOME%\resources\log4j.properties %EXEC_HOME%\dpft-engine.jar
echo %RERUN_SCRIPT_PATH%
GOTO:LOOP

:START_PROG_RERUN
echo START_PROG_RERUN Load script Path = %RERUN_SCRIPT_PATH%
start java -XX:-UseGCOverheadLimit -Xmx20G -Xms20G -Xmn5G -XX:PermSize=128m -XX:MaxPermSize=256m -XX:+UseG1GC -XX:ParallelGCThreads=20 -XX:MaxGCPauseMillis=200 -XX:ConcGCThreads=5 -XX:InitiatingHeapOccupancyPercent=70 -jar -Dlog4j.configuration=file:%EXEC_HOME%\resources\log4j.properties %EXEC_HOME%\dpft-engine.jar -script %RERUN_SCRIPT_PATH%

:LOOP
timeout /t 60
echo This is Loop!
for /f  "useback tokens=* delims=" %%# in (
    `wmic process where "CommandLine like '%%dpft-engine.jar%%' and not CommandLine like '%%wmic%%' " get  ProcessId /Format:Value`
) do (
    for /f "tokens=* delims=" %%a in ("%%#") do set "%%a"
)
echo ProcessId=%processId%
if "%processId%" == "" (
    echo Automation Engine Not Running...Check Condition before Restarting...
) else (
    echo Automation Engine is Running...Keep Monitoring
)


:CHKCONDITION2
echo CHKCONDITION2
for /f "useback delims=" %%b in (
    `sqlplus -s unicadbt/UNICADBT@172.17.241.236:5211/tacrm @"D:\IBM\DPFT\Runtime-Zack\resources\scripts\usp_checkcondition2.sql"`
) do (
    set rtnV=%%b
    echo rtnV for Condition2 = "%rtnV%"
    if "%rtnV%" == "1" (GOTO:START_PROG_CONDITION2) else GOTO:LOOP
)

:START_PROG_CONDITION2
echo START_PROG_CONDITION2
if NOT "%processId%" == "" (
    echo Automation Engine is Running...Kill Task PID = %processId%
    pause
    taskkill /f /PID %processId%
)
set RERUN_SCRIPT_PATH=%EXEC_HOME%\resources\scripts\automation_cond2.properties
GOTO:START_PROG_RERUN

pause