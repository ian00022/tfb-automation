package com.ibm.tfb.ext.action;

import java.util.HashMap;

import com.ibm.dpft.engine.core.action.DPFTActionObndPeriodicFileOutput;
import com.ibm.dpft.engine.core.util.DPFTFileFTPUtil;
import com.ibm.dpft.engine.core.util.DPFTFileFormatter;



public class QdmSvActionDataFileOutput extends DPFTActionObndPeriodicFileOutput {
	@Override
	public String getChannelName() {
		// TODO Auto-generated method stub
		return "QDM_Q";
	}

	@Override
	public boolean needNotification() {
		return true;
	}

	@Override
	public HashMap<DPFTFileFormatter, DPFTFileFTPUtil> getAdditionalDataFormatters(){
		return null;
	/*	FileMetaDefDboSet meta1 = null;
		FileDictionaryDboSet dicSet1 = null;
		HashMap<DPFTFileFormatter, DPFTFileFTPUtil> fmtrs = new HashMap<DPFTFileFormatter, DPFTFileFTPUtil>();

			DPFTConnector connector;
			try {
				connector = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig());
				meta1 = (FileMetaDefDboSet) connector.getDboSet("DPFT_FILE_META_DEF", "chal_name='QDM_Q' and active=1");
				dicSet1 = (FileDictionaryDboSet) connector.getDboSet("DPFT_FILE_DIC", "chal_name='QDM_Q' and active=1");
			} catch (DPFTConnectionException e) {
				throw new DPFTActionException(this, "SYSTEM", "DPFT0001E", e);
			}
			meta1.load();
			dicSet1.load();
			meta1.close();
			dicSet1.close();
			
			DPFTCSVFileFormatter fmtr = new DPFTCSVFileFormatter(new DPFTFileMetaData(meta1), new DPFTFileMetaData(meta1, dicSet1));
			fmtr.setFileEncoding(meta1.getFileEncoding());
			fmtrs.put(fmtr, new DPFTcdFTPUtil(meta1.getLocalDir(), meta1.getRemoteDir(), meta1.getCDProfile()));

		return fmtrs;*/
	}

}