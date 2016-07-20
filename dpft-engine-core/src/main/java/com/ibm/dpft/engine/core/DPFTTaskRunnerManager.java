package com.ibm.dpft.engine.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ibm.dpft.engine.core.auto.DPFTAutomationTaskRunner;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.taskplan.DPFTBaseTaskPlan;
import com.ibm.dpft.engine.core.taskplan.DPFTTaskPlan;
import com.ibm.dpft.engine.core.util.DPFTLogger;

public class DPFTTaskRunnerManager {
	private DPFTScheduler scheduler = null;
	private HashMap<String, DPFTTaskRunner> system_daemons = null;
	private HashMap<String, Integer> runner_instance_index = new HashMap<String, Integer>();

	public DPFTTaskRunnerManager(DPFTScheduler scheduler) {
		// TODO Auto-generated constructor stub
		this.scheduler = scheduler;
	}

	public void initialize(String engine_stat) {
		// TODO Auto-generated method stub
		if(scheduler == null)
			return;
		
		createInitialDaemon(engine_stat);
	}
	
	public void exec() {
		// TODO Auto-generated method stub
		DPFTLogger.info(this, "System daemons starting...");
		start(system_daemons);
	}

	public void start(HashMap<String, DPFTTaskRunner> runnerlist) {
		// TODO Auto-generated method stub
		for(String runnerid: runnerlist.keySet()){
			runnerlist.get(runnerid).start();
		}
	}
	
	public void start(ArrayList<DPFTTaskRunner> runnerlist) {
		// TODO Auto-generated method stub
		for(DPFTTaskRunner runner: runnerlist){
			runner.start();
		}
	}

	private void createInitialDaemon(String engine_stat) {
		// TODO Auto-generated method stub
		DPFTLogger.info(this, "Setting up initial daemon task runner...");
		if(engine_stat.equals(GlobalConstants.DPFT_ENGINE_STAT_PASSIVE))
			system_daemons = getThreadRunners(scheduler.getPassiveTaskPlan());
		else
			system_daemons = getThreadRunners(scheduler.getSystemTaskPlan());
	}

	private HashMap<String, DPFTTaskRunner> getThreadRunners(List<DPFTTaskPlan> tplist) {
		// TODO Auto-generated method stub
		HashMap<String, DPFTTaskRunner> runnerlist = new HashMap<String, DPFTTaskRunner>();
		for(DPFTTaskPlan tp: tplist){
			if(!runnerlist.containsKey(((DPFTBaseTaskPlan)tp).getId())){
				String tpid = ((DPFTBaseTaskPlan)tp).getId();
				String runnerId = getNextRunnerId(tpid);
				runnerlist.put(runnerId, new DPFTTaskRunner((DPFTBaseTaskPlan) tp, this));
			}
		}
		return runnerlist;
	}

	private String getNextRunnerId(String tpid) {
		// TODO Auto-generated method stub
		if(!runner_instance_index.containsKey(tpid)){
			runner_instance_index.put(tpid, 1);
			return tpid + "." + String.valueOf(1);
		}
		int i = runner_instance_index.get(tpid);
		i++;
		runner_instance_index.put(tpid, i);
		return tpid + "." + String.valueOf(i);
	}

	public DPFTTaskRunner createNewRunnerByTaskPlanID(String tpid) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		return addThreadRunner(scheduler.getRegisteredTaskPlanById(tpid), tpid);
	}

	public DPFTTaskRunner addThreadRunner(DPFTTaskPlan tp, String tpid) throws DPFTRuntimeException {
		if(tp == null){
			Object[] params = {tpid};
			throw new DPFTRuntimeException("SYSTEM", "DPFT0016E", params);
		}
		
		if(tpid.equals(GlobalConstants.DPFT_AUTOMATION_PS_MGR_ID)){
			DPFTAutomationTaskRunner runner = new DPFTAutomationTaskRunner((DPFTBaseTaskPlan)tp);
			system_daemons.put(tpid, runner);
			return runner;
		}else{
			String runnerid = getNextRunnerId(tpid);
			DPFTTaskRunner runner = new DPFTTaskRunner((DPFTBaseTaskPlan)tp, this);
			system_daemons.put(runnerid, runner);
			return runner;
		}
	}

	public void stopAllRunners() {
		DPFTLogger.info(this, "Task Manager stopping all running thread...");
		for(String runnerid: system_daemons.keySet()){
			DPFTTaskRunner runner = system_daemons.get(runnerid);
			if(runner.isAlive()){
				//Task still running, interrupt...
				DPFTLogger.info(this, "Task Runner : " + runnerid + " is still running. Force stopping...");
				runner.interrupt();
			}
		}
		
	}

	public void clearDeamonsQueue() {
		DPFTLogger.info(this, "Task Manager clearing system daemon queue...");
		system_daemons.clear();
		runner_instance_index.clear();
	}

	public DPFTAutomationTaskRunner getAutomationManager() {
		return (DPFTAutomationTaskRunner) system_daemons.get(GlobalConstants.DPFT_AUTOMATION_PS_MGR_ID);
	}
}
