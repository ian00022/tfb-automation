package com.ibm.tfb.ext.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.ibm.dpft.engine.core.DPFTEngine;
import com.ibm.dpft.engine.core.action.DPFTActionTableWatch;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTInboundControlDboSet;
import com.ibm.dpft.engine.core.util.DPFTLogger;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.tfb.ext.common.TFBConstants;
import com.ibm.tfb.ext.common.TFBUtil;
import com.ibm.tfb.ext.dbo.MKTDMCustomerContactDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDbo;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDboSet;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;

public class BdmActionCustContInfoDMWatch extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		// TODO Auto-generated method stub
		return TFBUtil.getMKTDMConfig();
	}

	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return DPFTEngine.getSystemProperties("mktdb.tbl.cust.cont");
	}

	@Override
	public String getTableWatchCriteria() throws DPFTRuntimeException {
		StringBuilder sb = new StringBuilder();
		sb.append("cust_id in (");
		sb.append(TFBUtil.getCustomerSelectINString(this.getPreviousAction().getResultSet(), "customer_id"));
		sb.append(") and cont_cd in ('" + TFBConstants.MKTDM_CONT_CD_ZIPCD_COMM + "','" + TFBConstants.MKTDM_CONT_CD_ADDR_COMM + "','" + TFBConstants.MKTDM_CONT_CD_ADDRCD_COMM + "','" + TFBConstants.MKTDM_CONT_CD_HOU_COMM + "')");
		DPFTLogger.info(this, "Table select where = " + sb.toString());
		return sb.toString();
	}

	@Override
	public String getTriggerKeyCol() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void postAction() throws DPFTRuntimeException {
		/*get D_BDM Set*/
		DPFTDboSet dBdmSet = this.getPreviousAction().getResultSet();
		
		/*Contact data from MKTDM*/
		MKTDMCustomerContactDboSet contSet = (MKTDMCustomerContactDboSet) this.getDataSet();
		
		/*set Addr by IBM Campaign Priority setting*/
		long ps_start_time = System.currentTimeMillis();
		for(int i = 0; i < dBdmSet.count(); i++){
			DPFTDbo dBdm = dBdmSet.getDbo(i);
			String[] addr_info = null;
			if(dBdm.isNull("addr_priority")){
				/*use default addr priority rule*/
				addr_info = contSet.getPrioritizedAddr(dBdm.getString("customer_id"), "BDM", GlobalConstants.DPFT_DEFAULT_PRIORITY_CODE);
			}else{
				/*use ADDR_PRIORITY Setting*/
				addr_info = contSet.getPrioritizedAddr(dBdm.getString("customer_id"), "BDM", dBdm.getString("addr_priority"));
			}
			dBdm.setValue("addr", addr_info[0]);
			dBdm.setValue("zip_cod", addr_info[1]);
			dBdm.setValue("addr_code", addr_info[2]);
			dBdm.setValue("branch_no", TFBUtil.getNextBranchNo(dBdm.getString("customer_id")));
			if((i+1)%100 == 0)
				DPFTLogger.debug(this, "Processed " + (i+1) + " records...");
		}
		
		/*pre-generate barcode*/
		ArrayList<Integer> need_barcode_dbo_idx = new ArrayList<Integer>();
		for(int i = 0; i < dBdmSet.count(); i++){
			DPFTDbo dBdm = dBdmSet.getDbo(i);
			if(dBdm.getString("has_bar_code").equalsIgnoreCase("y") && isValidCustInfo(dBdm)){
				need_barcode_dbo_idx.add(i);
			}else{
				dBdm.setValue("bill_seq", null);
				dBdm.setValue("bill_type", null);
				dBdm.setValue("bill_month", null);
			}
		}
		String[] barcode_list = TFBUtil.generateBarCode(need_barcode_dbo_idx.size());
		
		/*set barcode*/
		for(int i = 0; i < barcode_list.length; i++){
			DPFTDbo dBdm = dBdmSet.getDbo(need_barcode_dbo_idx.get(i));
			String next_seq = barcode_list[i];
			dBdm.setValue("bill_seq", next_seq.substring(1));
			if(next_seq != null)
				dBdm.setValue("bill_type", next_seq.substring(0, 1));
			dBdm.setValue("bill_month", TFBUtil.getROCYearMonthString(dBdm.getDate("timestamp")));
		}
		long ps_fin_time = System.currentTimeMillis();
		DPFTLogger.info(this, "Processed total " + dBdmSet.count() + ", process time = " + (ps_fin_time - ps_start_time)/60000 + " min.");
		
		/*Set Query criteria for "O_BDM"*/
		String qString = DPFTUtil.getFKQueryString(dBdmSet.getDbo(0));
		if(qString == null){
			DPFTLogger.debug(this, "Built FK Query String Failed...");
			return;
		}
		DPFTLogger.debug(this, "Built FK Query String = " + qString);
		
		/*Get Data set from "O_BDM"*/
		DPFTConnector connector = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig());
		DPFTOutboundDboSet oBdmSet = (DPFTOutboundDboSet) connector.getDboSet("O_BDM", qString);
		oBdmSet.load();
		/*Data set from "O_BDM" should be empty*/
		if(oBdmSet.count() > 0){
			DPFTLogger.info(this, "Records exist in output data set...Delete All Records...");
			oBdmSet.deleteAll();
		}
		
		Date current_timestamp = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DFPT_DATETIME_FORMAT);
		String data_yyyymm = sdf.format(current_timestamp).substring(0, 6);
		String data_date = sdf.format(current_timestamp).substring(0, 8);
		String data_time = sdf.format(current_timestamp).substring(8, 14);
		ArrayList<String> cell_code_list = new ArrayList<String>();
		ArrayList<String> cell_name_list = new ArrayList<String>();
		for(int i = 0; i < dBdmSet.count(); i++){
			DPFTOutboundDbo new_dbo = (DPFTOutboundDbo) oBdmSet.add();
			new_dbo.setValue(dBdmSet.getDbo(i));
			new_dbo.setValue("data_yyyymm", data_yyyymm);
			new_dbo.setValue("data_date"  , data_date);
			new_dbo.setValue("data_time"  , data_time);
			if(!isValidCustInfo(new_dbo) || (new_dbo.getString("has_bar_code").equalsIgnoreCase("y") && new_dbo.isNull("bill_seq"))){
				//Exclude records that have no Address, or need barcode but without Sequence
				new_dbo.setValue("process_status", GlobalConstants.O_DATA_EXCLUDE);
			}else{
				new_dbo.setValue("process_status", GlobalConstants.O_DATA_OUTPUT);
			}
			//find distinct cell code
			if(!cell_code_list.contains(new_dbo.getString("cell_code"))){
				cell_code_list.add(new_dbo.getString("cell_code"));
			}
			if(!cell_name_list.contains(new_dbo.getString("cellname"))){
				cell_name_list.add(new_dbo.getString("cellname"));
			}
		}
		oBdmSet.setRefresh(false);
		oBdmSet.save();
		
		/*Write usage codes to O_USAGECODE Table*/
		TFBUtil.processUsageCode(oBdmSet, "BDM");
		
		/*Write results to H_OUTBOUND Table*/ 
		TFBUtil.generateObndCtrlRecord(connector, oBdmSet, cell_code_list, cell_name_list, "BDM", true);
		oBdmSet.close();
	}

	private boolean isValidCustInfo(DPFTDbo dBdm) {
		// TODO Auto-generated method stub
		//return !dBdm.isNull("addr") && !dBdm.isNull("addr_code");
		return !dBdm.isNull("addr") ;
	}

	@Override
	public void handleException(DPFTActionException e) throws DPFTRuntimeException {
		DPFTInboundControlDboSet hIbndSet = (DPFTInboundControlDboSet) DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig())
				.getDboSet("H_INBOUND", DPFTUtil.getFKQueryString(((DPFTActionTableWatch)this.getPreviousAction()).getResultSet().getDbo(0)));
		hIbndSet.error();
		hIbndSet.close();
		throw e;		
	}

}
