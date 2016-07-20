package com.ibm.dpft.engine.core.action;

import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.dbo.ResFileDirSettingDbo;
import com.ibm.dpft.engine.core.dbo.ResFileDirSettingDboSet;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.dpft.engine.core.util.DPFTcdFTPUtil;

public class DPFTActionRemoteFileWatch extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		return DPFTUtil.getSystemDBConfig();
	}

	@Override
	public String getTableName() {
		return "DPFT_RES_FTP_DIR_DEF";
	}

	@Override
	public String getTableWatchCriteria() {
		return "active=1";
	}

	@Override
	public String getTriggerKeyCol() {
		return null;
	}

	@Override
	public void postAction() throws DPFTRuntimeException {
		ResFileDirSettingDboSet fSet = (ResFileDirSettingDboSet) this.getDataSet();
		for(int i = 0; i < fSet.count(); i++){
			try{
				ResFileDirSettingDbo dbo = (ResFileDirSettingDbo) fSet.getDbo(i);
				DPFTcdFTPUtil cdutil = new DPFTcdFTPUtil(dbo.getString("ldir")
													   , dbo.getString("dir")
													   , dbo.getString("cd_profile"));
				String[] flist = dbo.getRemoteFileName();
				String[] clist = dbo.getRemoteCtrlName();
				cdutil.doFTP_Get(clist, flist);
			}catch(Exception e){
				if(e instanceof DPFTRuntimeException){
					((DPFTRuntimeException)e).handleException();
				}else{
					DPFTRuntimeException ex = new DPFTRuntimeException("SYSTEM", "DPFT0008E", e);
					ex.handleException();
				}
			}
		}
	}

	@Override
	public void handleException(DPFTActionException e) throws DPFTActionException {
		// TODO Auto-generated method stub
		
	}

}
