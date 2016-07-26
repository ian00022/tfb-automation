package com.ibm.dpft.engine.core.auto.util.macro;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import com.ibm.dpft.engine.core.DPFTTaskRunner;
import com.ibm.dpft.engine.core.auto.DPFTAutomationTaskRunner;
import com.ibm.dpft.engine.core.auto.util.DPFTAutomationMacro;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.FileDictionaryDboSet;
import com.ibm.dpft.engine.core.dbo.FileMetaDefDboSet;
import com.ibm.dpft.engine.core.exception.DPFTAutomationException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.meta.DPFTFileMetaData;
import com.ibm.dpft.engine.core.taskplan.DPFTTaskPlan;
import com.ibm.dpft.engine.core.util.DPFTCSVFileFormatter;
import com.ibm.dpft.engine.core.util.DPFTLogger;
import com.ibm.dpft.engine.core.util.DPFTMessage;
import com.ibm.dpft.engine.core.util.DPFTUtil;

public class MacroVar extends DPFTAutomationMacro {
	private static HashMap<String, Object> cache_vars = new HashMap<String, Object>();
	
	public int loadMessage(String gid, String msgid) throws DPFTRuntimeException{
		return loadMessage(gid, msgid, null);
	}
	
	public int loadMessage(String gid, String msgid, String params) throws DPFTRuntimeException{
		Object[] ps = null;
		if(params != null){
			ps = params.split(GlobalConstants.FILE_DELIMETER_COMMA);
		}
		if(ps == null)
			setMacroReturnData(new DPFTMessage(gid, msgid));
		else
			setMacroReturnData(new DPFTMessage(gid, msgid, ps));
		return GlobalConstants.DPFT_AUTOMATION_PS_RC_NORMAL;
	}
	
	public int loadFileFormatter(FileMetaDefDboSet meta, FileDictionaryDboSet dicSet) throws DPFTRuntimeException{
		DPFTCSVFileFormatter formatter = new DPFTCSVFileFormatter(new DPFTFileMetaData(meta), new DPFTFileMetaData(meta, dicSet));
		formatter.setFileEncoding(meta.getFileEncoding());
		setMacroReturnData(formatter);
		return GlobalConstants.DPFT_AUTOMATION_PS_RC_NORMAL;
	}
	
	public int loadFileDictionary(String meta_id) throws DPFTRuntimeException{
		DPFTConnector connector = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig());
		FileDictionaryDboSet dicSet = (FileDictionaryDboSet) connector.getDboSet("DPFT_FILE_DIC", "chal_name='" + meta_id + "' and active=1");
		dicSet.load();
		dicSet.close();
		setMacroReturnData(dicSet);
		return GlobalConstants.DPFT_AUTOMATION_PS_RC_NORMAL;
	}
	
	public int loadFileMeta(String meta_id) throws DPFTRuntimeException{
		DPFTConnector connector = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig());
		FileMetaDefDboSet meta = (FileMetaDefDboSet) connector.getDboSet("DPFT_FILE_META_DEF", "chal_name='" + meta_id + "' and active=1");
		meta.load();
		meta.close();
		setMacroReturnData(meta);
		return GlobalConstants.DPFT_AUTOMATION_PS_RC_NORMAL;
	}
	
	public int loadTaskRunner(String id, String classname) throws DPFTRuntimeException {
		try {
			DPFTTaskPlan tp = (DPFTTaskPlan) Class.forName(classname).getConstructor(String.class).newInstance(id);
			DPFTTaskRunner runner = ((DPFTAutomationTaskRunner)DPFTAutomationTaskRunner.currentThread()).getMgr().addThreadRunner(tp, tp.getId());
			setMacroReturnData(runner);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			Object[] params = {classname};
			throw new DPFTAutomationException("SYSTEM", "AUTO0016E", params, e);
		}
		return GlobalConstants.DPFT_AUTOMATION_PS_RC_NORMAL;
	}
	
	public int loadTaskRunner(String tpid) throws DPFTRuntimeException {
		DPFTTaskRunner runner = ((DPFTAutomationTaskRunner)DPFTAutomationTaskRunner.currentThread()).getMgr().createNewRunnerByTaskPlanID(tpid);
		setMacroReturnData(runner);
		return GlobalConstants.DPFT_AUTOMATION_PS_RC_NORMAL;
	}
	
	public int loadSQL(String dbname, String script) throws DPFTRuntimeException {
		return loadSQL(null, dbname, script);
	}
	
	public int loadSQL(String mapping, String dbname, String script) throws DPFTRuntimeException {
		DPFTLogger.debug(this, "Invoke " + this.getInvokeMethod() + " in MACRO VAR");
		DPFTConnector connector = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getDBConfig(dbname), false);
		try {
			DPFTDboSet dataSet = connector.execSQL(script);
			dataSet.close();
			if(mapping == null){
				setMacroReturnData(dataSet);
			}else{
				setMacroReturnData(dataSet.getDbo(getMappingIndex(mapping)).getString(getMappingColumn(mapping)));
			}
			return GlobalConstants.DPFT_AUTOMATION_PS_RC_NORMAL;
		} catch (Exception e) {
			throw new DPFTAutomationException("SYSTEM", "AUTO0017E", e);
		}
	}
	
	public int subVar(String value) {
		setMacroReturnData(value);
		return GlobalConstants.DPFT_AUTOMATION_PS_RC_NORMAL;
	}
	
	public static int Cache(String varname, Object value){
		cache_vars.put(varname, value);
		return GlobalConstants.DPFT_AUTOMATION_PS_RC_NORMAL;
	}
	
	public static int Cache(String varname, String value){
		cache_vars.put(varname, value);
		return GlobalConstants.DPFT_AUTOMATION_PS_RC_NORMAL;
	}
	
	public static int RemoveCache(String varname){
		cache_vars.remove(varname);
		return GlobalConstants.DPFT_AUTOMATION_PS_RC_NORMAL;
	}

	private String getMappingColumn(String mapping) {
		String[] maps = mapping.split(GlobalConstants.FILE_DELIMETER_DOT);
		if(maps[0].equalsIgnoreCase(GlobalConstants.DPFT_AUTOMATION_MACRO_VARS_RS)){
			return maps[1];
		}
		return null;
	}

	private int getMappingIndex(String mapping) {
		return 0;
	}

	public static Object getCacheVar(String varname) {
		return cache_vars.get(varname);
	}

	public static int clearCache() {
		cache_vars.clear();
		return GlobalConstants.DPFT_AUTOMATION_PS_RC_NORMAL;
	}
}
