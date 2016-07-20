package com.ibm.dpft.engine.core.dbo;

import java.util.HashMap;

import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;

public class UnicaCampaignDboSet extends DPFTDboSet {

	public UnicaCampaignDboSet(DPFTConnector conn, String tbname, String whereclause) throws DPFTRuntimeException {
		super(conn, tbname, whereclause);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected DPFTDbo getDboInstance(String dboname, HashMap<String, Object> d) {
		return new UnicaCampaignDbo(dboname, d, this);
	}

	public UnicaCampaignDbo getCampaign(String cmp_code) throws DPFTRuntimeException {
		for(int i = 0; i < count(); i++){
			if(this.getDbo(i).isNull("campaigncode"))
				continue;
			
			if(this.getDbo(i).getString("campaigncode").equals(cmp_code))
				return (UnicaCampaignDbo) this.getDbo(i);
		}
		return null;
	}

}
