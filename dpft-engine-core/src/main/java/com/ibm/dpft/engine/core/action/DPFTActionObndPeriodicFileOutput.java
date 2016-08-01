package com.ibm.dpft.engine.core.action;

import java.util.ArrayList;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.FTPConfig;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTTriggerMapDefDbo;
import com.ibm.dpft.engine.core.dbo.DPFTTriggerMapDefDboSet;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTFileFTPUtil;
import com.ibm.dpft.engine.core.util.DPFTMessage;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.dpft.engine.core.util.DPFTcdFTPUtil;

public abstract class DPFTActionObndPeriodicFileOutput extends DPFTActionDataFileOutput {
	abstract public boolean needNotification();
	
	
	@Override
	public FTPConfig getFTPConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DPFTFileFTPUtil getFTPUtil() throws DPFTRuntimeException {
		return new DPFTcdFTPUtil(getOutFileLocalDir(), getOutFileRemoteDir(), meta.getCDProfile());
	}
	
	@Override
	public void finish() throws DPFTRuntimeException {	
		DPFTTriggerMapDefDbo tmap = (DPFTTriggerMapDefDbo) this.getInitialData();
		DPFTTriggerMapDefDboSet tmapSet = tmap.getControlTableRecords();
		tmapSet.updateLastActiveTime();
		tmapSet.save();
		
		if(needNotification()){
			for(String sf: success_ftp_files){
				Object[] params = {sf, this.getOutFileRemoteDir()};
				DPFTUtil.pushNotification(
						getCampaignOwners(), 
						new DPFTMessage("CUSTOM", "TFB00011I", params)
				);
			}
		}
		success_ftp_files.clear();
	}
	
	private String getCampaignOwners() throws DPFTRuntimeException {
		DPFTOutboundDboSet rSet = (DPFTOutboundDboSet) this.getPreviousAction().getResultSet();
		ArrayList<String> cmplist = new ArrayList<String>();
		for(int i = 0; i < rSet.count(); i++){
			if(cmplist.contains(rSet.getDbo(i).getString("camp_code")))
				continue;
			cmplist.add(rSet.getDbo(i).getString("camp_code"));
		}
		
		StringBuilder sb = new StringBuilder();
		for(String cmp_code: cmplist){
			sb.append(DPFTUtil.getCampaignOwnerEmail(cmp_code)).append(GlobalConstants.FILE_DELIMETER_COMMA);
		}
		if(sb.length() > 0)
			return sb.substring(0, sb.length()-1);
		return null;
	}


	@Override
	public void handleException(DPFTActionException e) throws DPFTRuntimeException {
		throw e;
	}

}
