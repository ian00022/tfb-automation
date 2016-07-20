package com.ibm.dpft.engine.core.util;

import org.apache.log4j.Logger;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;

public class DPFTLogger {

	public static void info(Object o ,String message) {
		// TODO Auto-generated method stub
		Logger logger = Logger.getLogger(o.getClass());
		logger.info(getTitleMessage(GlobalConstants.MSG_TITLE_01,message));
	}
	
	public static void info(String classname ,String message) {
		// TODO Auto-generated method stub
		Logger logger = Logger.getLogger(classname);
		logger.info(getTitleMessage(GlobalConstants.MSG_TITLE_01,message));
	}
	
	public static void debug(Object o ,String message) {
		// TODO Auto-generated method stub
		Logger logger = Logger.getLogger(o.getClass());
		logger.debug(getTitleMessage(GlobalConstants.MSG_TITLE_01,message));
	}
	
	public static void debug(String classname ,String message) {
		// TODO Auto-generated method stub
		Logger logger = Logger.getLogger(classname);
		logger.debug(getTitleMessage(GlobalConstants.MSG_TITLE_01,message));
	}
	
	public static void error(Object o ,String message) {
		// TODO Auto-generated method stub
		Logger logger = Logger.getLogger(o.getClass());
		logger.error(getTitleMessage(GlobalConstants.MSG_TITLE_01,message));
	}
	
	public static void error(Object o ,String message, Exception e) {
		// TODO Auto-generated method stub
		Logger logger = Logger.getLogger(o.getClass());
		logger.error(getTitleMessage(GlobalConstants.MSG_TITLE_01,message), e);
	}
	
	public static void error(String classname ,String message) {
		// TODO Auto-generated method stub
		Logger logger = Logger.getLogger(classname);
		logger.error(getTitleMessage(GlobalConstants.MSG_TITLE_01,message));
	}

	public static void error(String classname, String message, Exception e) {
		// TODO Auto-generated method stub
		Logger logger = Logger.getLogger(classname);
		logger.error(getTitleMessage(GlobalConstants.MSG_TITLE_01,message), e);
	}

	private static String getTitleMessage(String title, String message) {
		// TODO Auto-generated method stub
		if(title == null || title.isEmpty())
			return message;
		
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(title).append("] (Thread Id: ").append(Thread.currentThread().getId() + ") ").append(message);
		return sb.toString();
	}

	public static void error(DPFTRuntimeException e) {
		Logger logger = Logger.getLogger(e.getClass().getName());
		logger.error(getTitleMessage(GlobalConstants.MSG_TITLE_01, e.getMessage()), e);
	}

}
