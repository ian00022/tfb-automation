package com.ibm.tfb.ext.action;

import java.util.HashMap;

import com.ibm.dpft.engine.core.action.DPFTActionDataFileOutput;
import com.ibm.dpft.engine.core.config.FTPConfig;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.meta.DPFTFileMetaData;
import com.ibm.dpft.engine.core.util.DPFTFileFTPUtil;
import com.ibm.dpft.engine.core.util.DPFTFileFormatter;
import com.ibm.dpft.engine.core.util.DPFTcdFTPUtil;
import com.ibm.tfb.ext.util.FCBFileFormatter;

public class EcbActionDataFileOutput extends DPFTActionDataFileOutput {
	@Override
	public DPFTFileFormatter getFileFormatter() throws DPFTRuntimeException {
		return new FCBFileFormatter(new DPFTFileMetaData(meta), new DPFTFileMetaData(meta, dicSet), "ECB_INFO");
	}

	@Override
	public HashMap<DPFTFileFormatter, DPFTFileFTPUtil> getAdditionalDataFormatters() {
		return null;
	}

	@Override
	public FTPConfig getFTPConfig() {
		return null;
	}

	@Override
	public DPFTFileFTPUtil getFTPUtil() throws DPFTRuntimeException {
		return new DPFTcdFTPUtil(getOutFileLocalDir(), getOutFileRemoteDir(), meta.getCDProfile());
	}

	@Override
	public String getChannelName() {
		return "ECB";
	}

}
