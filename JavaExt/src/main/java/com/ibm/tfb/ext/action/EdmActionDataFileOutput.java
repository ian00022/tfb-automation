package com.ibm.tfb.ext.action;

import java.util.HashMap;

import com.ibm.dpft.engine.core.action.DPFTActionDataFileOutput;
import com.ibm.dpft.engine.core.config.FTPConfig;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTFileFTPUtil;
import com.ibm.dpft.engine.core.util.DPFTFileFormatter;
import com.ibm.dpft.engine.core.util.DPFTcdFTPUtil;

public class EdmActionDataFileOutput extends DPFTActionDataFileOutput {
	@Override
	public String getChannelName() {
		// TODO Auto-generated method stub
		return "EDM";
	}

	@Override
	public FTPConfig getFTPConfig() {
		// TODO Auto-generated method stub
		FTPConfig config = new FTPConfig();
		config.setHost("10.211.55.6");
		config.setPort(21);
		config.setUser("Administrator");
		config.setPassword("p@ssw0rd");
		return config;
	}

	@Override
	public DPFTFileFTPUtil getFTPUtil() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		return new DPFTcdFTPUtil(getOutFileLocalDir(), getOutFileRemoteDir(), meta.getCDProfile());
	}

	@Override
	public HashMap<DPFTFileFormatter, DPFTFileFTPUtil> getAdditionalDataFormatters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleException(DPFTActionException e) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		
	}

}
