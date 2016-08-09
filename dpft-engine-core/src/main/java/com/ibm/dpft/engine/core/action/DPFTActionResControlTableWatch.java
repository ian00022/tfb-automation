package com.ibm.dpft.engine.core.action;

import java.util.HashMap;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.dbo.DPFTInboundControlDbo;
import com.ibm.dpft.engine.core.dbo.DPFTInboundControlDboSet;
import com.ibm.dpft.engine.core.dbo.ResFileDirSettingDbo;
import com.ibm.dpft.engine.core.dbo.ResFileDirSettingDboSet;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTFileReadException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTFileReader;
import com.ibm.dpft.engine.core.util.DPFTUtil;

public class DPFTActionResControlTableWatch extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		// TODO Auto-generated method stub
		return DPFTUtil.getSystemDBConfig();
	}

	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return "H_INBOUND_RES";
	}

	@Override
	public String getTableWatchCriteria() {
		// TODO Auto-generated method stub
		return "process_status='" + GlobalConstants.DPFT_CTRL_STAT_RUN + "'";
	}

	@Override
	public String getTriggerKeyCol() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void postAction() throws DPFTRuntimeException {
		DPFTInboundControlDboSet hSet = (DPFTInboundControlDboSet) this.getDataSet();
		if(hSet.isEmpty()){
			return;
		}
		
		ResFileDirSettingDboSet fSet = (ResFileDirSettingDboSet) this.getPreviousAction().getResultSet();
		HashMap<String, DPFTFileReader> drs = new HashMap<String, DPFTFileReader>();
		for(int i = 0; i < fSet.count(); i++){
			ResFileDirSettingDbo f = (ResFileDirSettingDbo) fSet.getDbo(i);
			drs.put(f.getDataFileName(), f.getLocalFileReader());
		}
		
		String timestamp = DPFTUtil.getCurrentTimeStampAsString();
		for(int i = 0; i < hSet.count(); i++){
			DPFTInboundControlDbo h = (DPFTInboundControlDbo) hSet.getDbo(i);
			String d_file_name = h.getString("d_file");
			DPFTFileReader data_reader = drs.get(d_file_name);
			if(data_reader == null)
				continue;
			try{
				if(!data_reader.exist(d_file_name)){
					Object[] params = {d_file_name};
					throw new DPFTFileReadException("CUSTOM", "TFB00014E", params);
				}
				
				if(data_reader.read(d_file_name)){
					data_reader.write2TargetTable(timestamp);
					h.setValue("process_time", timestamp);
					h.complete();
				}
			}catch(Exception e){
				h.error();
				if(e instanceof DPFTRuntimeException){
					((DPFTRuntimeException) e).handleException();
				}else{
					Object[] params = {d_file_name};
					DPFTFileReadException ex = new DPFTFileReadException("SYSTEM", "DPFT0032E", params, e);
					ex.handleException();
				}
			}
			
		}
		hSet.save();
	}

	@Override
	public void handleException(DPFTActionException e) throws DPFTActionException {
		this.changeActionStatus(GlobalConstants.DPFT_ACTION_STAT_COMP);
	}

}
