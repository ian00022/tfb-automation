package com.ibm.tfb.ext.action;

import java.util.HashMap;

import com.ibm.dpft.engine.core.action.DPFTActionDataFileOutput;
import com.ibm.dpft.engine.core.config.FTPConfig;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.meta.DPFTFileMetaData;
import com.ibm.dpft.engine.core.util.DPFTFileFTPUtil;
import com.ibm.dpft.engine.core.util.DPFTFileFormatter;
import com.ibm.dpft.engine.core.util.DPFTcdFTPUtil;
import com.ibm.tfb.ext.common.TFBUtil;
import com.ibm.tfb.ext.util.SMSFileFormatter;

public class EsmActionDataFileOutput extends DPFTActionDataFileOutput {
	@Override
	public DPFTFileFormatter getFileFormatter() throws DPFTRuntimeException {
		//GroupID&UserName&Password&OrderTime&Exprie&MsgType&Priority&MID&ObjectID&ContactEmail
		String[] header = {"GROUPID", "USERNAME", "PASSWORD", "ORDERTIME", "EXPIRE", "MSGTYPE", "PRIORITY", "MID", "OBJECTID", "CONTACTEMAIL"};
		return new SMSFileFormatter(new DPFTFileMetaData(meta), new DPFTFileMetaData(meta, dicSet), TFBUtil.buildHeaderString(header, this.getPreviousAction().getResultSet().getDbo(0)));
	}

	@Override
	public HashMap<DPFTFileFormatter, DPFTFileFTPUtil> getAdditionalDataFormatters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FTPConfig getFTPConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DPFTFileFTPUtil getFTPUtil() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		return new DPFTcdFTPUtil(getOutFileLocalDir(), getOutFileRemoteDir(), meta.getCDProfile());
	}

	@Override
	public String getChannelName() {
		// TODO Auto-generated method stub
		return "ESM";
	}

	@Override
	public void handleException(DPFTActionException e) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		
	}

}
