package com.ibm.tfb.ext.action;

import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTOutboundDbo;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;

public class SfaLzActionDataTableWatch extends LzActionDataTableWatch {

	@Override
	protected void setMoreValue(DPFTOutboundDbo new_dbo, DPFTDbo dbo) {
		new_dbo.setValue("sfa_lead_id", new_dbo.getString("customer_id") + "||" + new_dbo.getString("treatment_code"));
//		if(dbo.getString("bu_code") != null){
//			//分別處理房信貸lead_type
//			new_dbo.setValue("lead_type", (dbo.getString("bu_code").equals("H"))?TFBConstants.SFA_LZ_LEADTYPE_HL:TFBConstants.SFA_LZ_LEADTYPE_PL);
//		}
	}
	
	@Override
	public String getTableWatchCriteria() throws DPFTRuntimeException {
		String where = super.getTableWatchCriteria();
		String chal_name = this.getInitialData().getString("chal_name");
		if(chal_name.indexOf("SFA_LZ_HL") != -1){
			where = where + " and bu_code='" + chal_name.substring(chal_name.length() - 1) + "'";
		}
		return where;
	}

}
