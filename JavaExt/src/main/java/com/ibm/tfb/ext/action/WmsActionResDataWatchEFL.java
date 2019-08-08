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

public class WmsActionResDataWatchEFL extends DPFTActionTableWatch {

	@Override
	public DPFTConfig getDBConfig() {
		return TFBUtil.getSSDOPConfig();
	}

	@Override
	public String getTableName() {
		return "CM_EFL_LEAD_RESP";
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

			DPFTDboSet eflSet = DPFTConnectionFactory.initDPFTConnector(DPFTUtil.getSystemDBConfig()).getDboSet("RSP_MAIN", "chal_name='EFL'");

			for (int i = 0; i < set.count(); i++) {

				DPFTDbo efl = eflSet.add();
				efl.setValue("TREATMENT_CODE", set.getDbo(i).getString("TREATMENT_CODE"));
				efl.setValue("CUSTOMER_ID", set.getDbo(i).getString("CUST_ID"));
				efl.setValue("RES_DATE", set.getDbo(i).getString("RESP_DATE"));
				efl.setValue("RES_CODE", set.getDbo(i).getString("RESP_CODE"));
				efl.setValue("RESV1", set.getDbo(i).getString("RESV1"));
				efl.setValue("RESV2", set.getDbo(i).getString("RESV2"));
			}

			eflSet.save();
			eflSet.close();
		}

	}

	@Override
	public void handleException(DPFTActionException e) throws DPFTRuntimeException {
		// TODO Auto-generated method stub

	}

}
