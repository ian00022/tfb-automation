package com.ibm.dpft.engine.core.dbo;

import java.util.HashMap;

import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;

public class DPFTArchiveDboSet extends DPFTDboSet {

	public DPFTArchiveDboSet(DPFTConnector conn, String tbname, String whereclause) throws DPFTRuntimeException {
		super(conn, tbname, whereclause);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected DPFTDbo getDboInstance(String dboname, HashMap<String, Object> d) {
		return new DPFTArchiveDbo(dboname, d, this);
	}

}
