package com.ibm.tfb.ext.action;

import java.util.HashMap;

import com.ibm.dpft.engine.core.action.DPFTActionDataFileOutput;
import com.ibm.dpft.engine.core.config.FTPConfig;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTFileFTPUtil;
import com.ibm.dpft.engine.core.util.DPFTFileFormatter;
import com.ibm.dpft.engine.core.util.DPFTcdFTPUtil;

public class LtmActionDataFileOutput extends DPFTActionDataFileOutput {

	@Override
	public HashMap<DPFTFileFormatter, DPFTFileFTPUtil> getAdditionalDataFormatters() {
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
		return "LTM";
	}

}
