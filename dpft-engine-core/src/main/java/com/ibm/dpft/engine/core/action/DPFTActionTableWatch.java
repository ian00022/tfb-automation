package com.ibm.dpft.engine.core.action;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTConnectionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTLogger;

public abstract class DPFTActionTableWatch extends DPFTAction implements DPFTTableWatchInterface{
	private DPFTConnector conn = null;
	private DPFTDboSet ds = null;
	private final static long sleep_time = 10000;
	
	public DPFTActionTableWatch(){
		super();
		/*default set watch intervals to 10 seconds*/
		this.setPostActionSleepTime(sleep_time);
	}
	
	void initDboSet() throws DPFTRuntimeException {
		try {
			ds = conn.getDboSet(this.getTableName(), this.getTableWatchCriteria());
		} catch (DPFTConnectionException e) {
			throw new DPFTActionException(this, "SYSTEM", "DPFT0003E", e);
		}
	}
	
	@Override
	public void action() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		if(!this.isActionRunning())
			throw new DPFTActionException(this, "SYSTEM", "DPFT0005E");
		
		if(conn == null)
			initDBConnector();
		
		initDboSet();
		if(ds.count() > 0){
			DPFTLogger.info(this, "Action: Watching Table = " + this.getTableName() + " has record to process...");
			this.setPostActionSleepTime(0);
		}else{
			DPFTLogger.info(this, "Action: Watching Table = " + this.getTableName() + " no record found...");
		}
		this.changeActionStatus(GlobalConstants.DPFT_ACTION_STAT_COMP);
		postAction();
	}
	
	@Override
	public void finish() throws DPFTRuntimeException {
		/*free connection resource*/
		ds.close();
		conn = null;
	}
	
	@Override
	public void clean() throws DPFTRuntimeException {
		if(ds != null){
			finish();
			ds.clear();
			ds = null;
		}
		if(this.getResultSet() != null){
			this.getResultSet().clear();
			this.setResultSet(null);
		}
	}
	
	void initDBConnector() throws DPFTRuntimeException {
		try {
			setDBConnector(DPFTConnectionFactory.initDPFTConnector(this.getDBConfig()));
		} catch (DPFTConnectionException e) {
			throw new DPFTActionException(this, "SYSTEM", "DPFT0003E", e);
		}
	}

	public DPFTConnector getDBConnector() {
		return conn;
	}

	void setDBConnector(DPFTConnector conn) {
		this.conn = conn;
	}

	public DPFTDboSet getDataSet() {
		return ds;
	}

}
