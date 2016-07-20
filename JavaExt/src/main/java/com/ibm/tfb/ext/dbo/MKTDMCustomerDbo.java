package com.ibm.tfb.ext.dbo;

import java.util.HashMap;

import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;

public class MKTDMCustomerDbo extends DPFTDbo {

	public MKTDMCustomerDbo(String dboname, HashMap<String, Object> data, DPFTDboSet thisSet) {
		super(dboname, data, thisSet);
		// TODO Auto-generated constructor stub
	}

	public boolean find(String cust_id) {
		// TODO Auto-generated method stub
		return this.getString("cust_id").equalsIgnoreCase(cust_id);
	}

}
