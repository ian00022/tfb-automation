package com.ibm.dpft.engine.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.dbo.SystemPropDboSet;
import com.ibm.dpft.engine.core.exception.DPFTInvalidSystemSettingException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTLogger;
import com.ibm.dpft.engine.core.util.DPFTUtil;

public class DPFTEngine {
	private DPFTConfig cfg = null;
	private static DPFTConnector connObj = null;
	private DPFTScheduler scheduler = null;
	private DPFTTaskRunnerManager taskrunnerMgr = null;
	private static Properties prop = null;
	private String stat = GlobalConstants.DPFT_ENGINE_STAT_INIT;
	private ArrayList<String> utplist = new ArrayList<String>();
	
	public DPFTEngine() throws DPFTRuntimeException {
		// TODO Auto-generated constructor stub
		DPFTLogger.info(this, "Engine instantiate Read System properties from config file...");
		prop = getConfigProperites();
		cfg = DPFTUtil.getSystemDBConfig();
	}

	public void initialize() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		if(cfg == null)
			return;
		
		DPFTLogger.info(this, "Engine instantiate...");
		DPFTLogger.info(this, "Engine try to connect to DB ...");
		connObj  = DPFTConnectionFactory.initDPFTConnector(cfg, false);
		DPFTConnector.setSystemDBConnectorInstance(connObj);
		DPFTLogger.info(this, "Engine successfully connected to DB...");
		
		DPFTLogger.info(this, "Engine load Remote properties from System Table...");
		loadRemoteSystemProperties();
		DPFTLogger.info(this, "Engine successfully load Remote properties...");
		
		DPFTLogger.info(this, "Engine Scheduler instantiate...");
		scheduler  = new DPFTScheduler(connObj);
		scheduler.registerTaskPlanFromDB();
		if(!utplist.isEmpty()){
			scheduler.setInitialTaskPlan(utplist.toArray(new String[utplist.size()]));
		}else{
			String[] tp = {GlobalConstants.DPFT_TP_PSV_AUTO_INIT};
			scheduler.setInitialTaskPlan(tp);
		}
		DPFTLogger.info(this, "Engine Scheduler successfully instantiated...");
		
		DPFTLogger.info(this, "Engine tasks runner manager instantiate...");
		taskrunnerMgr  = new DPFTTaskRunnerManager(scheduler);
		taskrunnerMgr.initialize();
		DPFTLogger.info(this, "Engine tasks runner manager successfully instantiate...");
		
	}
	
	private void loadRemoteSystemProperties() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		SystemPropDboSet propSet = (SystemPropDboSet) connObj.getDboSet("DPFT_SYSTEM_PROP", "active=1");
		for(int i = 0; i < propSet.count(); i++){
			String rprop = propSet.getDbo(i).getString("prop");
			String value = propSet.getDbo(i).getString("value");
			if(!prop.containsKey(rprop)){
				DPFTLogger.info(this, "Loading Remote Properties " + rprop + ", value = " + value);
				if(value == null)
					prop.put(rprop, "");
				else
					prop.put(rprop, value);
			}
		}
	}

	public static String getSystemProperties(String key){
		return prop.getProperty(key);
	}

	private Properties getConfigProperites() throws DPFTRuntimeException {
		if(prop != null)
			return prop;
		InputStream input = null;
		try {
			input = this.getClass().getClassLoader().getResourceAsStream("config.properties");
			Properties p = new Properties();
			p.load(input);
			return p;
		} catch (IOException e) {
			throw new DPFTRuntimeException("SYSTEM", "DPFT0034E");
		} finally {
			if(input != null){
				try {
					input.close();
				} catch (IOException e) {
					throw new DPFTRuntimeException("SYSTEM", "DPFT0034E");
				}
			}
		}
	}

	public void start() {
		// TODO Auto-generated method stub
		DPFTLogger.info(this, "Engine Starting...");
		taskrunnerMgr.exec();
		stat = GlobalConstants.DPFT_ENGINE_STAT_RUN;
		DPFTLogger.info(this, "Engine successfully started...");
	}

	public void stop() {
		// TODO Auto-generated method stub
		DPFTLogger.info(this, "Engine Stopping...");
		
	}
	
//	public void startAutomationMode() throws DPFTRuntimeException {
//		DPFTLogger.info(this, "Engine Switching to Automation Mode...");
//		taskrunnerMgr.stopAllRunners();
//		taskrunnerMgr.clearDeamonsQueue();
//		stat = GlobalConstants.DPFT_ENGINE_STAT_PASSIVE;
//		DPFTLogger.info(this, "Engine initializing system deamons for Automation Mode...");
//		taskrunnerMgr.initialize(stat);
//		DPFTLogger.info(this, "Engine Starting in Automation Mode...");
//		taskrunnerMgr.exec();
//		DPFTLogger.info(this, "Engine successfully started in Automation Mode...");
//		
//		updateSystemProp(GlobalConstants.DPFT_SYS_PROP_AUTO_ENABLE_TIME, getNextAutomationTrgTime());
//	}

	public static void updateSystemProp(String p, String value) throws DPFTRuntimeException {
		SystemPropDboSet propSet = (SystemPropDboSet) connObj.getDboSet("DPFT_SYSTEM_PROP", "prop='" + p + "' and active=1");
		if(propSet.isEmpty()){
			Object[] params = {p};
			throw new DPFTInvalidSystemSettingException("SYSTEM", "DPFT0035E", params);
		}
		propSet.getDbo(0).setValue("value", value);
		propSet.save();
		propSet.close();
		prop.put(p, value);
	}

//	private String getNextAutomationTrgTime() throws DPFTRuntimeException {
//		SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DFPT_DATETIME_FORMAT);
//		Calendar cal = Calendar.getInstance();
//		try {
//			Date trg_time = DPFTUtil.convertDateString2DateObject(getSystemProperties(GlobalConstants.DPFT_SYS_PROP_AUTO_ENABLE_TIME));
//			cal.setTime(trg_time);
//			cal.add(Calendar.DAY_OF_MONTH, 1);
//			return sdf.format(cal.getTime());
//		} catch (Exception e) {
//			throw new DPFTRuntimeException("SYSTEM", "AUTO0025E", e);
//		}
//	}

//	public boolean isAutomationModeActive() throws DPFTRuntimeException {
//		Date current_time = new Date();
//		try {
//			Date trg_time = DPFTUtil.convertDateString2DateObject(getSystemProperties(GlobalConstants.DPFT_SYS_PROP_AUTO_ENABLE_TIME));
//			return current_time.after(trg_time) 
//					&& !stat.equals(GlobalConstants.DPFT_ENGINE_STAT_PASSIVE) 
//					&& DPFTEngine.getSystemProperties(GlobalConstants.DPFT_SYS_PROP_AUTO_ENABLE).equalsIgnoreCase("y");
//		} catch (Exception e) {
//			throw new DPFTRuntimeException("SYSTEM", "AUTO0025E", e);
//		}
//	}

	public void add2UserDefinedTaskPlanList(String tp) {
		utplist.add(tp);
	}

//	public boolean isResume() {
//		if(taskrunnerMgr.getAutomationManager() == null)
//			return false;
//		return !taskrunnerMgr.getAutomationManager().isActive();
//	}

//	public void resume() {
//		DPFTLogger.info(this, "Engine Resuming to Default Running Mode...");
//		taskrunnerMgr.stopAllRunners();
//		taskrunnerMgr.clearDeamonsQueue();
//		stat = GlobalConstants.DPFT_ENGINE_STAT_RUN;
//		DPFTLogger.info(this, "Engine initializing system deamons for Default Running Mode...");
//		taskrunnerMgr.initialize(stat);
//		DPFTLogger.info(this, "Engine Starting...");
//		taskrunnerMgr.exec();
//		DPFTLogger.info(this, "Engine successfully started...");
//		
//	}

}
