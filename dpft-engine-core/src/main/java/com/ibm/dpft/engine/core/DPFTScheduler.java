package com.ibm.dpft.engine.core;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.dbo.TaskPlanDefDboSet;
import com.ibm.dpft.engine.core.exception.DPFTInvalidSystemSettingException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.dpft.engine.core.taskplan.DPFTTaskPlan;
import com.ibm.dpft.engine.core.util.DPFTLogger;

public class DPFTScheduler {
	private List<DPFTTaskPlan> tplist = new ArrayList<DPFTTaskPlan>();
	private List<DPFTTaskPlan> deamon_tplist = new ArrayList<DPFTTaskPlan>();
	private DPFTConnector connector = null;

	public DPFTScheduler(DPFTConnector connObj) {
		// TODO Auto-generated constructor stub
		connector  = connObj;
	}

	public void registerTaskPlan(DPFTTaskPlan tp) {
		DPFTLogger.debug(this, "Registered Task Plan class : " + tp.getClass().getName());
		tplist.add(tp);
	}

	public void registerTaskPlanFromDB() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		DPFTLogger.info(this, "Start loading task plan from DB...");
		TaskPlanDefDboSet planSet = (TaskPlanDefDboSet) connector.getDboSet("DPFT_TASKPLAN_DEF", "active=?");
		planSet.setBoolean(1, true);
		planSet.load();
		try {
			List<DPFTTaskPlan> taskplans = planSet.convert2TaskPlanList();
			for(DPFTTaskPlan tp : taskplans){
				registerTaskPlan(tp);
			}
		} catch (Exception e) {
			throw new DPFTRuntimeException("SYSTEM", "DPFT0036E", e);
		} 
		DPFTLogger.info(this, "DB Task Plan definitions sucessfully registered...");
	}

	private void registerInitialTaskPlan(DPFTTaskPlan tp) {
		DPFTLogger.debug(this, "Registered Inital Task Plan class : " + tp.getClass().getName());
		deamon_tplist.add(tp);
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
		return deamon_tplist;
	}

	public void setInitialTaskPlan(String[] tplist) throws DPFTRuntimeException {
		for(String tp: tplist){
			DPFTTaskPlan init_plan = getRegisteredTaskPlanById(tp);
			if(init_plan == null)
				init_plan = loadTaskPlanByClassName(tp);
			registerInitialTaskPlan(init_plan);
		}
	}

	private DPFTTaskPlan loadTaskPlanByClassName(String tp) throws DPFTRuntimeException {
		try {
			return (DPFTTaskPlan) Class.forName(tp).getConstructor(String.class).newInstance(tp);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			Object[] params = {tp};
			throw new DPFTInvalidSystemSettingException("SYSTEM", "DPFT0016E", params, e);
		}
	}

	public List<DPFTTaskPlan> getInitialTaskPlan() {
		return deamon_tplist;
	}

}
