package com.ibm.dpft.engine.core.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.ibm.dpft.engine.core.DPFTEngine;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.exception.DPFTConnectionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTLogger;

public class DPFTConnectionFactory {

	private static Connection getDBConnectionInstance(DPFTConfig cfg) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			throw new DPFTConnectionException("SYSTEM", "COMM0002E", e);
		}
		
		Connection conn = null;
		Properties prop = new Properties();
		int retry_limit = Integer.valueOf(DPFTEngine.getSystemProperties(GlobalConstants.DPFT_SYS_PROP_CONN_RETRY));
		int retry_num = 0;
		while(conn == null){
			try {
//				DPFTLogger.debug(DPFTConnectionFactory.class.getName(), "JDBC Connection = " + cfg.getConnectionString());
//				DPFTLogger.debug(DPFTConnectionFactory.class.getName(), "DB User = " + cfg.getDBUserName());
//				DPFTLogger.debug(DPFTConnectionFactory.class.getName(), "DB Password = " + cfg.getDBPassword());
//				DPFTLogger.debug(DPFTConnectionFactory.class.getName(), "Connect to DB as " + cfg.getDBUserName());
				prop.setProperty("user", cfg.getDBUserName());
				prop.setProperty("password", cfg.getDBPassword());
				prop.setProperty("oracle.jdbc.ReadTimeout", "14400000");
				prop.setProperty("oracle.net.CONNECT_TIMEOUT", "14400000");
				conn = DriverManager.getConnection(cfg.getConnectionString(), prop);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				DPFTLogger.error(DPFTConnectionFactory.class.getName(), "SQL Error when getting connection instance:", e);
				if(retry_num <= retry_limit){
					try {
						//Retry after 1 min.
						retry_num++;
						Thread.sleep(60000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}else{
					throw new DPFTConnectionException("SYSTEM", "COMM0004E", e);
				}
			}
		}
		return conn;
	}
	
	public static DPFTConnector initDPFTConnector(DPFTConfig cfg, boolean closed) throws DPFTRuntimeException {
		DPFTConnector connector = new DPFTConnector(getDBConnectionInstance(cfg), cfg);
		if(closed){
			try {
				connector.close();
			} catch (SQLException e) {
				throw new DPFTConnectionException("SYSTEM", "COMM0003E", e);
			}
		}
		return connector;
	}
	
	public static DPFTConnector initDPFTConnector(DPFTConfig cfg) throws DPFTRuntimeException {
		return initDPFTConnector(cfg, true);
	}
}
