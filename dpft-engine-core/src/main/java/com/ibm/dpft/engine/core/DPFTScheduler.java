package com.ibm.dpft.engine.core;

import java.util.ArrayList;
import java.util.List;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.dbo.TaskPlanDefDboSet;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.dpft.engine.core.taskplan.DPFTTaskPlan;
import com.ibm.dpft.engine.core.util.DPFTLogger;

public class DPFTScheduler {
	private List<DPFTTaskPlan> tplist = new ArrayList<DPFTTaskPlan>();
	private List<DPFTTaskPlan> system_tplist = new ArrayList<DPFTTaskPlan>();
	private List<DPFTTaskPlan> passive_tplist = new ArrayList<DPFTTaskPlan>();
	private DPFTConnector connector = null;

	public DPFTScheduler(DPFTConnector connObj) {
		// TODO Auto-generated constructor stub
		connector  = connObj;
	}

	public void registerTaskPlan(DPFTTaskPlan tp, boolean isDaemonPlan) {
		// TODO Auto-generated method stub
		if(isDaemonPlan){
			DPFTLogger.debug(this, "Registered Startup Engine Task Plan class : " + tp.getClass().getName());
			system_tplist.add(tp);
		}else{
			DPFTLogger.debug(this, "Registered Task Plan class : " + tp.getClass().getName());
			tplist.add(tp);
		}
	}

	public void registerTaskPlanFromDB() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		DPFTLogger.info(this, "Start loading task plan from DB...");
		TaskPlanDefDboSet planSet = (TaskPlanDefDboSet) connector.getDboSet("DPFT_TASKPLAN_DEF", "active=?");
		planSet.setBoolean(1, true);
		planSet.load();
		planSet.close();
		try {
			List<DPFTTaskPlan> taskplans = planSet.convert2TaskPlanList();
			for(DPFTTaskPlan tp : taskplans){
				if(tp.getId().indexOf(GlobalConstants.DPFT_TP_KEY_PSV) != -1)
					registerPassiveTaskPlan(tp);
				else
					registerTaskPlan(tp, false);
			}
		} catch (Exception e) {
			throw new DPFTRuntimeException("SYSTEM", "DPFT0036E", e);
		} 
		DPFTLogger.info(this, "DB Task Plan definitions sucessfully registered...");
	}

	private void registerPassiveTaskPlan(DPFTTaskPlan tp) {
		DPFTLogger.debug(this, "Registered Passive Task Plan class : " + tp.getClass().getName());
		passive_tplist.add(tp);
	}

	public List<DPFTTaskPlan> getSystemTaskPlan() {
		// TODO Auto-generated method stub
		return system_tplist;
	}
	
	public DPFTTaskPlan getRegisteredTaskPlanById(String tpid) {
		// TODO Auto-generated method stub
		for(DPFTTaskPlan tp: tplist){
			if(((DPFTBaseTaskPlan)tp).getId().equalsIgnoreCase(tpid))
				return tp;
		}
		return null;
	}

	public List<DPFTTaskPlan> getPassiveTaskPlan() {
		// TODO Auto-generated method stub
		return passive_tplist;
	}

}
