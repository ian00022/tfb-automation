package com.ibm.tfb.ext.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.ibm.dpft.engine.core.action.DPFTActionTableWatch;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.config.DPFTConfig;
import com.ibm.dpft.engine.core.connection.DPFTConnectionFactory;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.DPFTResMainDboSet;
import com.ibm.dpft.engine.core.exception.DPFTActionException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.tfb.ext.common.TFBUtil;

public class WmsActionResDataWatchMVP extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		return TFBUtil.getMTSFAConfig();
	}

	@Override
	public String getTableName() {
		return "CM_MVP_LEAD_RESP";
	}

	@Override
	public String getTableWatchCriteria() {
		SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DFPT_DATE_FORMAT);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		String t_minus_1 = sdf.format(cal.getTime());
		cal.add(Calendar.DAY_OF_MONTH, -1);
		String t_minus_2 = sdf.format(cal.getTime());
		return "RESP_DATE >= to_date('" + t_minus_2 + "','YYYYMMDD') and RESP_DATE < to_date('" + t_minus_1
				+ "', 'YYYYMMDD') and CUST_ID is not null";
	}

	@Override
	public String getTriggerKeyCol() {
		return null;
	}

	@Override
	public void postAction() throws DPFTRuntimeException {
		DPFTDboSet set = this.getDataSet();
		if (!set.isEmpty()) {

			DPFTDboSet mvpSet = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig()).getDboSet("RSP_MAIN", "chal_name='MVP'");

			for (int i = 0; i < set.count(); i++) {

				DPFTDbo mvp = mvpSet.add();
				mvp.setValue("TREATMENT_CODE", set.getDbo(i).getString("SFA_LEAD_ID"));
				mvp.setValue("CUSTOMER_ID", set.getDbo(i).getString("CUST_ID"));
				mvp.setValue("RES_DATE", set.getDbo(i).getString("RESP_DATE"));
				mvp.setValue("RES_CODE", set.getDbo(i).getString("RESP_CODE"));
				mvp.setValue("ORIG_RESP_CODE", set.getDbo(i).getString("ORIG_RESP_CODE"));
				mvp.setValue("RESV1", set.getDbo(i).getString("RESV1"));
				mvp.setValue("RESV2", set.getDbo(i).getString("RESV2"));
			}

			mvpSet.save();
			mvpSet.close();
		}

	}

	@Override
	public void handleException(DPFTActionException e) throws DPFTRuntimeException {
		// TODO Auto-generated method stub

	}

}