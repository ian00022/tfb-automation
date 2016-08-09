package com.ibm.dpft.engine.core.dbo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.exception.DPFTDboException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTLogger;
import com.ibm.dpft.engine.core.util.DPFTUtil;

public class DPFTTriggerMapDefDbo extends DPFTDbo {
	private Date current_time = null;
	private String trg_time_string = null;

	public DPFTTriggerMapDefDbo(String dboname, HashMap<String, Object> data, DPFTDboSet thisSet) {
		super(dboname, data, thisSet);
		// TODO Auto-generated constructor stub
	}

	public boolean canInvoke() throws DPFTRuntimeException {
		if(this.isNull("trg_time"))
			return false;
		current_time = new Date();
		try{
			if(this.isNull("frequency") || this.getString("frequency").equals(GlobalConstants.DPFT_OBND_FEQ_DAILY))
				return passTriggerTime() && pass24Hours();
		}catch(Exception e){
			throw new DPFTDboException("SYSTEM", "DPFT0028E", e);
		}
		return false;
	}

	private boolean pass24Hours() throws ParseException {
		if(this.isNull("last_active_time"))
			return true;
		SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DFPT_DATETIME_FORMAT);
		Date last_active_time = sdf.parse(this.getString("last_active_time"));
		long diff = current_time.getTime() - last_active_time.getTime();
		long hour_diff = diff/(3600*1000);
		if(hour_diff < 24){
			DPFTLogger.debug(this, "Task Plan : " + this.getString("planid") + " remaining " + (24-hour_diff) + " hours to be invoked...");
			return false;
		}
		DPFTLogger.info(this, "Daily Task Plan : " + this.getString("planid") + " is preparing to run...");
		return true;
	}

	private boolean passTriggerTime() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DFPT_DATE_FORMAT);
		trg_time_string = sdf.format(current_time) + this.getString("trg_time");
		SimpleDateFormat sdf1 = new SimpleDateFormat(GlobalConstants.DFPT_DATETIME_FORMAT);
		if(current_time.after(sdf1.parse(trg_time_string))){
			DPFTLogger.debug(this, "Current time of the day pass Trigger Time...");
			return true;
		}
		DPFTLogger.debug(this, "Current time of the day do not pass Trigger Time...");
		return false;
	}

	public String getDataSelectCriteria() {
		//to_timestamp(process_time, 'YYYYMMDDHH24MISS') between to_timestamp('20160505192000', 'YYYYMMDDHH24MISS') and to_timestamp('20160506192000', 'YYYYMMDDHH24MISS')
		StringBuilder sb = new StringBuilder();
		sb.append("to_timestamp(process_time, '").append(GlobalConstants.ORA_DB_TIME_FORMAT)
		.append("') between to_timestamp('").append(this.getString("last_active_time")).append("', '").append(GlobalConstants.ORA_DB_TIME_FORMAT)
		.append("') and to_timestamp('").append(trg_time_string).append("', '").append(GlobalConstants.ORA_DB_TIME_FORMAT)
		.append("') and process_status='").append(GlobalConstants.O_DATA_OUTPUT).append("'");
		return sb.toString();
	}
	
	public String getDataSelectCriteria(String process_status) {
		StringBuilder sb = new StringBuilder();
		sb.append("to_timestamp(process_time, '").append(GlobalConstants.ORA_DB_TIME_FORMAT)
		.append("') between to_timestamp('").append(this.getString("last_active_time")).append("', '").append(GlobalConstants.ORA_DB_TIME_FORMAT)
		.append("') and to_timestamp('").append(trg_time_string).append("', '").append(GlobalConstants.ORA_DB_TIME_FORMAT)
		.append("') and process_status='").append(process_status).append("'");
		return sb.toString();
	}
	
	public String getTrgTimeString() {
		return trg_time_string;
	}

	public DPFTTriggerMapDefDboSet getControlTableRecords() throws DPFTRuntimeException {
		StringBuilder sb = new StringBuilder();
		sb.append("chal_name='").append(this.getString("chal_name"))
		.append("' and planid='").append(this.getString("planid"))
		.append("' and active=0");
		DPFTTriggerMapDefDboSet rtnSet = (DPFTTriggerMapDefDboSet) DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig()).getDboSet(this.getDboName(), sb.toString());
		rtnSet.setParent(this);
		return rtnSet;
	}

}
