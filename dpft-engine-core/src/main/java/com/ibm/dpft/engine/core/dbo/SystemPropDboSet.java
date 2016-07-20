package com.ibm.dpft.engine.core.dbo;

import java.util.HashMap;

import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;

public class SystemPropDboSet extends DPFTDboSet {

	public SystemPropDboSet(DPFTConnector conn, String tbname, String whereclause) throws DPFTRuntimeException {
		super(conn, tbname, whereclause);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected DPFTDbo getDboInstance(String dboname, HashMap<String, Object> d) {
		return new SystemPropDbo(dboname, d, this);
	}

	public String getPropValue(String prop) throws DPFTRuntimeException {
		for(int i = 0; i < this.count(); i++){
			if(prop.equalsIgnoreCase(this.getDbo(i).getString("prop"))){
				return this.getDbo(i).getString("value");
			}
		}
		return null;
	}
}
