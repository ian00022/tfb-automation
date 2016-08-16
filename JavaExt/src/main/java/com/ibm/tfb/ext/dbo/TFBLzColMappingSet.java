package com.ibm.tfb.ext.dbo;

import java.util.HashMap;

import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;

public class TFBLzColMappingSet extends DPFTDboSet {

	public TFBLzColMappingSet(DPFTConnector conn, String tbname, String whereclause) throws DPFTRuntimeException {
		super(conn, tbname, whereclause);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected DPFTDbo getDboInstance(String dboname, HashMap<String, Object> d) {
		return new TFBLzColMapping(dboname, d, this);
	}

	public HashMap<String, String> getTargetColumnMapping() throws DPFTRuntimeException {
		HashMap<String, String> rtnMap = new HashMap<String, String>();
		for(int i = 0; i < count(); i++){
			rtnMap.put(this.getDbo(i).getString("target_col"), this.getDbo(i).getString("source_col"));
		}
		return rtnMap;
	}
}
