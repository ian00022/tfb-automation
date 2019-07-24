set serveroutput on size 30000;
declare
	OutputStr VARCHAR(5);
BEGIN
	SELECT (CASE WHEN Z.CNT>0 THEN 1 ELSE 0 END) INTO OutputStr FROM (
  	SELECT count(*) as cnt 
  	FROM unicadbt.UA_CCRUNLOG A
  WHERE A.RUNSTATUS='Running'
  AND extract(hour from (CURRENT_TIMESTAMP - A.RUNSTARTTIME))>=4
  AND EXTRACT(HOUR FROM CURRENT_TIMESTAMP)<8) Z;

	DBMS_OUTPUT.PUT_LINE(OutputStr);
END;
/
exit