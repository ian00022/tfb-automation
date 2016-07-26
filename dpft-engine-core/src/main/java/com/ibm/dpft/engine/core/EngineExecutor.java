package com.ibm.dpft.engine.core;

import java.lang.reflect.InvocationTargetException;

import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTLogger;

public class EngineExecutor {

	private static DPFTEngine engine;
	private static int mode = 0;
	private static final int ARG_OPTION_R = 1;
	private static final int ARG_OPTION_ERR = -1;

	public static void main(String[] args) throws DPFTRuntimeException{	
		/*set engine db connection properties*/
		engine = new DPFTEngine();
		for(String arg: args){
			switch(arg.charAt(0)){
			case '-':
				processArguments(arg);
				break;
			default:
				processArgumentValues(arg);
			}
		}
		if(mode == ARG_OPTION_ERR)
			System.exit(ARG_OPTION_ERR);
		try {
			_init();
		} catch (DPFTRuntimeException e1) {
			System.exit(ARG_OPTION_ERR);
		}
		
//		do{
//			if(engine.isAutomationModeActive()){
//				try {
//					engine.startAutomationMode();
//				} catch (DPFTRuntimeException e) {
//					DPFTLogger.error(EngineExecutor.class.getName(), "Runtime Error: ", e);
//				}
//			}
//			if(engine.isResume()){
//				engine.resume();
//			}
//		}while(true);
	}

	private static void processArgumentValues(String arg) {
		switch(mode){
		case ARG_OPTION_R:
				engine.add2UserDefinedTaskPlanList(arg);
		}
	}

	private static void processArguments(String arg) {
		switch(arg.substring(1)){
		case "run":
			mode = ARG_OPTION_R;
			break;
		default:
			DPFTLogger.error(EngineExecutor.class.getName(), "Passing Argument is not valid...");
			mode = ARG_OPTION_ERR;
		}
	}

	private static void _init() throws DPFTRuntimeException {
		engine.initialize();
//		if(engine.isAutomationModeActive())
//			engine.startAutomationMode();
//		else
			engine.start();	
	}
}
