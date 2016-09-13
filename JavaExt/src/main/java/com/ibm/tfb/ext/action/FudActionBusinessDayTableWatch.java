package com.ibm.tfb.ext.action;

import com.ibm.dpft.engine.core.DPFTEngine;
import com.ibm.dpft.engine.core.action.DPFTActionTableWatch;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTInboundControlDboSet;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTUtil;

public class FudActionBusinessDayTableWatch extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		return DPFTUtil.getSystemDBConfig();
	}

	@Override
	public String getTableName() {
		return DPFTEngine.getSystemProperties(""); //BUSINESS DAY TABLE
	}

	@Override
	public String getTableWatchCriteria() throws DPFTRuntimeException {
		return "";
	}

	@Override
	public String getTriggerKeyCol() {
		return null;
	}

	@Override
	public void postAction() throws DPFTRuntimeException {
		// TODO Auto-generated constructor stub
		
		//get D_file dataSet
		DPFTDboSet dFudSet = ((DPFTActionTableWatch)this.getPreviousAction()).getDataSet();
		
//		
//		DPFTOutboundDboSet oLesSet = (DPFTOutboundDboSet) connector.getDboSet("O_FUD", qString);
//		ArrayList<String> cell_code_list = new ArrayList<String>();
//		oLesSet.load();
//		/*Data set from "O_EDM" should be empty*/
//		if(oLesSet.count() > 0){
//			DPFTLogger.info(this, "Records exist in output data set...Delete All Records...");
//			oLesSet.deleteAll();
//		}
	}

	@Override
	public void handleException(DPFTActionException e) throws DPFTRuntimeException {
		DPFTInboundControlDboSet hIbndSet = (DPFTInboundControlDboSet) DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig())
				.getDboSet("H_INBOUND", DPFTUtil.getFKQueryString(((DPFTActionTableWatch)this.getPreviousAction()).getDataSet().getDbo(0)));
		hIbndSet.error();
		hIbndSet.close();
		throw e;
	}

}
