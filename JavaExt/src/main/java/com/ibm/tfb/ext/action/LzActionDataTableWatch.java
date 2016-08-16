package com.ibm.tfb.ext.action;

import java.util.HashMap;

import com.ibm.dpft.engine.core.action.DPFTActionTableWatch;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTInboundControlDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDbo;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDboSet;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTInvalidSystemSettingException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTLogger;
import com.ibm.dpft.engine.core.util.DPFTMessage;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.tfb.ext.common.TFBConstants;
import com.ibm.tfb.ext.common.TFBUtil;
import com.ibm.tfb.ext.dbo.TFBLzColMappingSet;

public abstract class LzActionDataTableWatch extends DPFTActionTableWatch {
	private TFBLzColMappingSet mSet = null;
	private String source_tbl = null;

	@Override
	public DPFTConfig getDBConfig() {
		return DPFTUtil.getSystemDBConfig();
	}

	@Override
	public String getTableName() {
		return source_tbl;
	}
	
	@Override
	public void action() throws DPFTRuntimeException {
		if(mSet == null)
			mSet = (TFBLzColMappingSet) DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig())
			.getDboSet("TFB_LZ_COL_MAP", "chal_name='" + this.getInitialData().getString("chal_name") + "'");
		if(mSet.isEmpty()){
			Object[] params = {this.getInitialData().getString("chal_name")};
			throw new DPFTInvalidSystemSettingException("CUSTOM", "TFB00015E", params);
		}
		source_tbl = mSet.getDbo(0).getString("source_tbl");
		super.action();
	}

	@Override
	public String getTableWatchCriteria() throws DPFTRuntimeException {
		return "data_datetime='" + this.getInitialData().getString("timestamp") + "'";
	}

	@Override
	public String getTriggerKeyCol() {
		return null;
	}

	@Override
	public void postAction() throws DPFTRuntimeException {
		Object[] params = {"留資"};
		if(this.getDataSet().isEmpty())
			throw new DPFTActionException(this, "CUSTOM", "TFB00001E", params);
		DPFTUtil.pushNotification(
				new DPFTMessage("CUSTOM", "TFB00008I", params)
		);
		
		/*Get Data set from Source Table*/
		DPFTDboSet dLzSet = this.getDataSet();
		
		String timestamp = this.getInitialData().getString("timestamp");
		String chal_name = this.getInitialData().getString("chal_name");
		
		/*Set Query criteria for target Table*/
		String qString = TFBUtil.buildLZQueryString(timestamp, chal_name);
		DPFTLogger.debug(this, "Built FK Query String = " + qString);
		
		/*Get Data set from Target Table*/
		String target_tbl = mSet.getDbo(0).getString("target_tbl");
		DPFTOutboundDboSet oSet = (DPFTOutboundDboSet) this.getDBConnector().getDboSet(target_tbl, qString);
		if(oSet.count() > 0){
			DPFTLogger.info(this, "Records exist in output data set...Delete All Records...");
			oSet.deleteAll();
		}
		
		/*pre-generate Cust ID, Treatment Code for 留資*/
		long ps_start_time = System.currentTimeMillis();
		String[] lz_cust_list = TFBUtil.generateSEQ(TFBConstants.SFA_LZ_CUST_SEQ_ID, dLzSet.count());
		String[] t_code_list = TFBUtil.generateSEQ(TFBConstants.SFA_LZ_TCODE_SEQ_ID, dLzSet.count());
		
		/*set Cust ID, Treatment Code for 留資*/
		for(int i = 0; i < dLzSet.count(); i++){
			DPFTDbo dLz = dLzSet.getDbo(i);
			dLz.setValue("customer_id", lz_cust_list[i]);
			dLz.setValue("treatment_code", t_code_list[i]); 
		}
		long ps_fin_time = System.currentTimeMillis();
		DPFTLogger.info(this, "Processed total " + dLzSet.count() + ", process time = " + (ps_fin_time - ps_start_time)/60000 + " min.");
		
		
		/*add record to outbound data table*/		
		HashMap<String, String> map = mSet.getTargetColumnMapping();
		for(int i = 0; i < dLzSet.count(); i++){
			DPFTOutboundDbo new_dbo = (DPFTOutboundDbo) oSet.add();
			new_dbo.setValue("timestamp", timestamp);
			new_dbo.setValue("chal_name", chal_name);
			new_dbo.setValue("customer_id", dLzSet.getDbo(i).getString("customer_id"));
			new_dbo.setValue("treatment_code", dLzSet.getDbo(i).getString("treatment_code"));
			new_dbo.setValue("cell_code", timestamp.substring(GlobalConstants.DFPT_DATE_FORMAT.length()));
			for(String target_col: map.keySet()){
				new_dbo.setValue(target_col, getSourceValue(dLzSet.getDbo(i), map.get(target_col)));
			}
			new_dbo.setValue("process_status", GlobalConstants.O_DATA_OUTPUT);
			setMoreValue(new_dbo, dLzSet.getDbo(i));
		}
		oSet.save(false);
		oSet.close();
		
		/*Write results to H_OUTBOUND Table*/
		TFBUtil.generateObndCtrlForLZRecord(this.getDBConnector(), chal_name, timestamp, target_tbl, oSet.count());
	}

	private Object getSourceValue(DPFTDbo dbo, String source_col) {
		if(source_col.indexOf("{") == 0 
				&& source_col.indexOf("}") == (source_col.length() - 1))
			return source_col.substring(1, source_col.length() - 1);
		return dbo.getString(source_col);
	}

	protected abstract void setMoreValue(DPFTOutboundDbo new_dbo, DPFTDbo dbo);

	@Override
	public void handleException(DPFTActionException e) throws DPFTRuntimeException {
		//set correspond h_inbound record to error
		String timestamp = this.getInitialData().getString("timestamp");
		String chal_name = this.getInitialData().getString("chal_name");
		
		DPFTInboundControlDboSet hIbndSet = (DPFTInboundControlDboSet) DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig())
													.getDboSet("H_INBOUND", TFBUtil.buildLZQueryString(timestamp, chal_name));
		hIbndSet.error();
		hIbndSet.close();
		throw e;
	}

}
