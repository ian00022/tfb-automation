package com.ibm.tfb.ext.action;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import com.ibm.dpft.engine.core.DPFTEngine;
import com.ibm.dpft.engine.core.action.DPFTActionObndPeriodicFileOutput;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTFileFTPUtil;
import com.ibm.dpft.engine.core.util.DPFTFileFormatter;
import com.ibm.tfb.ext.common.TFBConstants;

public class EblActionDataFileOutput extends DPFTActionObndPeriodicFileOutput {

	@Override
	public HashMap<DPFTFileFormatter, DPFTFileFTPUtil> getAdditionalDataFormatters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean needNotification() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getChannelName() {
		// TODO Auto-generated method stub
		return "EBL";
	}
	
	@Override
	public void finish() throws DPFTRuntimeException {	
		super.finish();
		updateBillMonth();
	}

	private void updateBillMonth() throws DPFTRuntimeException {
		SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DPFT_MONTH_FORMAT);
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(sdf.parse(DPFTEngine.getSystemProperties(TFBConstants.EBL_PROP_BILL_MONTH)));
			cal.add(Calendar.MONTH, 1);
			DPFTEngine.updateSystemProp(TFBConstants.EBL_PROP_BILL_MONTH, sdf.format(cal.getTime()));
		} catch (ParseException e) {
			throw new DPFTRuntimeException("CUSTOM", "TFB00017E", e);
		}
		
	}

}
