package com.ibm.tfb.ext.dbo;

import java.util.HashMap;

import com.ibm.dpft.engine.core.DPFTEngine;
import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTLogger;
import com.ibm.dpft.engine.core.util.DPFTUtil;
import com.ibm.tfb.ext.common.TFBInvalidCustInfoException;

public class MKTDMCustomerDboSet extends DPFTDboSet {

	public MKTDMCustomerDboSet(DPFTConnector conn, String tbname, String whereclause) throws DPFTRuntimeException {
		super(conn, tbname, whereclause);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected DPFTDbo getDboInstance(String dboname, HashMap<String, Object> d) {
		return new MKTDMCustomerDbo(dboname, d, this);
	}
	
	private String getCustInfo(String cust_id, String colname) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		for(int i = 0; i < this.count(); i++){
			MKTDMCustomerDbo dbo = (MKTDMCustomerDbo) this.getDbo(i);
			if(dbo.find(cust_id)){
				return dbo.getString(colname);
			}
		}
		return null;
	}

	public String getCustName(String cust_id) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		return getCustInfo(cust_id, "CUST_NM_CUST");
	}

	public String getSysNo(String cust_id) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		return getCustInfo(cust_id, "INSSEQ");
	}

	public String getGender(String cust_id) throws DPFTRuntimeException {
		String cust_info = getCustInfo(cust_id, "GNDR_CUST");
		if(cust_info == null){
			DPFTLogger.error(this, "Gender is null for cust_id = " + cust_id);
			return null;
		}
		
		if(cust_info.equals("1"))
			return DPFTEngine.getSystemProperties("tfb.gndr.male");
		else if(cust_info.equals("2"))
			return DPFTEngine.getSystemProperties("tfb.gndr.female");
		Object[] params = {cust_info};
		throw new TFBInvalidCustInfoException("CUSTOM", "TFB00002E", params);
	}

	public String getDateAsString(String cust_id, String colname) throws DPFTRuntimeException {
		for(int i = 0; i < this.count(); i++){
			MKTDMCustomerDbo dbo = (MKTDMCustomerDbo) this.getDbo(i);
			if(dbo.find(cust_id)){
				return DPFTUtil.convertDateObject2DateString(dbo.getDate(colname), GlobalConstants.DFPT_DATE_FORMAT);
			}
		}
		return null;
	}
}
