package com.ibm.tfb.ext.dbo;

import java.util.HashMap;

import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;

public class MKTDMCustomerContactDbo extends DPFTDbo {

	public MKTDMCustomerContactDbo(String dboname, HashMap<String, Object> data, DPFTDboSet thisSet) {
		super(dboname, data, thisSet);
		// TODO Auto-generated constructor stub
	}

	public boolean find(String cust_id, String cd) {
		// TODO Auto-generated method stub
		return (this.getString("cust_id").equalsIgnoreCase(cust_id) && this.getString("cont_cd").equalsIgnoreCase(cd));
	}

	public boolean find(String cust_id, String cont_cd, String biz_type) {
		// TODO Auto-generated method stub
		return find(cust_id, cont_cd) && this.getString("biz_cat").equalsIgnoreCase(biz_type);
	}

}
