package com.ibm.tfb.ext.action;

import java.util.ArrayList;

import com.ibm.dpft.engine.core.DPFTEngine;
import com.ibm.dpft.engine.core.action.DPFTActionTableWatch;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTInboundControlDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDbo;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDboSet;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTLogger;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.tfb.ext.common.TFBUtil;
import com.ibm.tfb.ext.dbo.MKTDMCustomerDboSet;

public class PcbActionCustInfoDMWatch extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		return TFBUtil.getMKTDMConfig();
	}

	@Override
	public String getTableName() {
		return DPFTEngine.getSystemProperties("mktdb.tbl.cust");
	}

	@Override
	public String getTableWatchCriteria() throws DPFTRuntimeException {
		StringBuilder sb = new StringBuilder();
		sb.append("cust_id in (");
		sb.append(TFBUtil.getCustomerSelectINString(((DPFTActionTableWatch)this.getPreviousAction()).getDataSet(), "customer_id"));
		sb.append(")");
		DPFTLogger.info(this, "Table select where = " + sb.toString());
		return sb.toString();
	}

	@Override
	public String getTriggerKeyCol() {
		return null;
	}

	@Override
	public void postAction() throws DPFTRuntimeException {
		/*get data set from D_PCB*/
		DPFTDboSet dPcbSet = ((DPFTActionTableWatch)this.getPreviousAction()).getDataSet();
		
		/*get data set from Customer info*/
		MKTDMCustomerDboSet custDboSet = (MKTDMCustomerDboSet) this.getDataSet();
		
		/*set value to personal info column*/
		for(int i = 0; i < dPcbSet.count(); i++){
			DPFTDbo dPcb = dPcbSet.getDbo(i);
			dPcb.setValue("sys_no"  , custDboSet.getSysNo(dPcb.getString("customer_id")));
		}
		
		/*Set Query criteria for "O_PCB"*/
		String qString = DPFTUtil.getFKQueryString(dPcbSet.getDbo(0));
		if(qString == null){
			DPFTLogger.debug(this, "Built FK Query String Failed...");
			return;
		}
		DPFTLogger.debug(this, "Built FK Query String = " + qString);
		
		/*Get Data set from "O_PCB"*/
		DPFTConnector connector = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig());
		DPFTOutboundDboSet oPcbSet = (DPFTOutboundDboSet) connector.getDboSet("O_PCB", qString);
		oPcbSet.load();
		/*Data set from "O_PCB" should be empty*/
		if(oPcbSet.count() > 0){
			DPFTLogger.info(this, "Records exist in output data set...Delete All Records...");
			oPcbSet.deleteAll();
		}
		
		ArrayList<String> cell_code_list = new ArrayList<String>();
		for(int i = 0; i < dPcbSet.count(); i++){
			DPFTOutboundDbo new_dbo = (DPFTOutboundDbo) oPcbSet.add();
			new_dbo.setValue(dPcbSet.getDbo(i));
			if(!isValidCustInfo(new_dbo)){
				//Exclude records
				new_dbo.setValue("process_status", GlobalConstants.O_DATA_EXCLUDE);
			}else{
				new_dbo.setValue("process_status", GlobalConstants.O_DATA_OUTPUT);
			}
			//find distinct cell code
			if(!cell_code_list.contains(new_dbo.getString("cell_code"))){
				cell_code_list.add(new_dbo.getString("cell_code"));
			}
		}
		oPcbSet.save();
		
		/*Write usage codes to O_USAGECODE Table*/
		TFBUtil.processUsageCode(oPcbSet, "PCB");
		
		/*Write results to H_OUTBOUND Table*/
		TFBUtil.generateObndCtrlRecord(connector, oPcbSet, cell_code_list, "PCB", true);
		oPcbSet.close();

	}

	private boolean isValidCustInfo(DPFTOutboundDbo new_dbo) {
		if(new_dbo.isNull("sys_no"))
			return false;
		return true;
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
