package com.ibm.dpft.engine.core;

import java.lang.reflect.InvocationTargetException;

import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTLogger;

public class EngineExecutor {

	private static DPFTEngine engine;
	private static int mode = 0;
	private static final int ARG_OPTION_R = 1;
	private static final int ARG_OPTION_S = 2;
	private static final int ARG_OPTION_ERR = -1;
	private static final String KEY_AUTOSCRIPT = "sys.auto.script";

	
	
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
		
		boolean isFatalError = false;
		do{
			try{
				if(isFatalError){
					DPFTLogger.info(EngineExecutor.class.getName(), "Engine encounter fatal Errors... Terminate Engine PROGRAM!!!");
					System.exit(ARG_OPTION_ERR);
				}
				if(engine.isAutomationModeActive()){
					engine.startAutomationMode();
				}
				if(engine.isResume()){
					engine.resume();
				}
			}catch(Exception e){
				if(e instanceof DPFTRuntimeException){
					try {
						((DPFTRuntimeException) e).handleException();
					} catch (Exception e1) {
						DPFTLogger.error(EngineExecutor.class.getName(), "Fatal Error", e1);
					}
				}else{
					DPFTLogger.error(EngineExecutor.class.getName(), "Uncaught Exception", e);
				}
				isFatalError = true;
			}
		}while(true);
	}

	private static void processArgumentValues(String arg) {
		switch(mode){
		case ARG_OPTION_R:
			try {
				engine.add2UserDefinedTaskPlanList(arg);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException
					| ClassNotFoundException e) {
				DPFTLogger.error(EngineExecutor.class.getName(), "Error when add user defined taskplan...", e);
				mode = ARG_OPTION_ERR;
			}
		case ARG_OPTION_S:
			try {
				DPFTEngine.setSystemProperty(KEY_AUTOSCRIPT, arg);
			}catch(Exception e) {
				DPFTLogger.error(EngineExecutor.class.getName(), "Error when set Custom Automation Script...", e);
				mode = ARG_OPTION_ERR;
			}
		}
		
	}

	private static void processArguments(String arg) {
		switch(arg.substring(1)){
		case "run":
			mode = ARG_OPTION_R;
			break;
		case "script":
			mode = ARG_OPTION_S;
		default:
			DPFTLogger.error(EngineExecutor.class.getName(), "Passing Argument is not valid...");
			mode = ARG_OPTION_ERR;
		}
	}

	private static void _init() throws DPFTRuntimeException {
		engine.initialize();
		if(engine.isAutomationModeActive())
			engine.startAutomationMode();
		else
			engine.start();	
	}
}
