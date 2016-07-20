package com.ibm.dpft.engine.core.dbo;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import com.ibm.dpft.engine.core.taskplan.DPFTTaskPlan;

public class TaskPlanDefDbo extends DPFTDbo {

	public TaskPlanDefDbo(String dboname, HashMap<String, Object> data, DPFTDboSet thisSet) {
		super(dboname, data, thisSet);
		// TODO Auto-generated constructor stub
	}

	public DPFTTaskPlan getDefTaskPlanClassInstance() throws InstantiationException, 
															 IllegalAccessException, 
															 ClassNotFoundException, 
															 IllegalArgumentException, 
															 InvocationTargetException, 
															 NoSuchMethodException, 
															 SecurityException {
		// TODO Auto-generated method stub
		String classname = this.getString("classname");
		String id = this.getString("planid");
		return (DPFTTaskPlan) Class.forName(classname).getConstructor(String.class).newInstance(id);
	}

}
