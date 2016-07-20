package com.ibm.dpft.engine.core.dbo;

import java.util.HashMap;

import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;

public class ResFileDataLayoutDboSet extends DPFTDboSet {

	public ResFileDataLayoutDboSet(DPFTConnector conn, String tbname, String whereclause) throws DPFTRuntimeException {
		super(conn, tbname, whereclause);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected DPFTDbo getDboInstance(String dboname, HashMap<String, Object> d) {
		return new ResFileDataLayoutDbo(dboname, d, this);
	}
}
