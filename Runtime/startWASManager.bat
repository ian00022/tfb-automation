#Script to start DPFT Engine

set PATH=%PATH%;C:\Program Files\Java\jdk1.7.0_79\bin
set EXEC_HOME=D:\IBM\DPFT\Runtime
java -jar -Dlog4j.configuration=file:%EXEC_HOME%\resources\log4j.properties %EXEC_HOME%\dpft-engine.jar -run com.ibm.dpft.engine.core.taskplan.DPFTWebServerCmdExecutor