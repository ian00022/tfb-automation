package com.ibm.tfb.ext.action;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundControlDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDboSet;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTLogger;
import com.ibm.dpft.engine.core.util.DPFTMessage;
import com.ibm.dpft.engine.core.util.DPFTUtil;

public class CtmLzActionDataFileOutput extends LzActionDataFileOutput {

	@Override
	public String getChannelName() {
		return "CTM_LZ_CC";
	}
	
	@Override
	public void finish() throws DPFTRuntimeException {		
		/*update Obnd Control Table Records*/
		DPFTLogger.info(this, "Updating Outbound Control Records...");
		DPFTOutboundDboSet rSet = (DPFTOutboundDboSet) this.getPreviousAction().getResultSet();
		DPFTOutboundControlDboSet ctrlSet = rSet.getLzControlTableRecords("process_status='" + GlobalConstants.DPFT_OBND_STAT_RUN + "'");
		ctrlSet.taskComplete();
		ctrlSet.close();
		
		//send Notification
		for(String sf: success_ftp_files){
			Object[] params = {sf, this.getOutFileRemoteDir()};
			DPFTUtil.pushNotification(
					new DPFTMessage("CUSTOM", "TFB00018I", params)
			);
		}
		success_ftp_files.clear();
	}
}
