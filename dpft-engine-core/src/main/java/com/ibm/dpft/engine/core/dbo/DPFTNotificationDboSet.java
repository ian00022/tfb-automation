package com.ibm.dpft.engine.core.dbo;

import java.util.HashMap;

import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;

public class DPFTNotificationDboSet extends DPFTDboSet {

	public DPFTNotificationDboSet(DPFTConnector conn, String tbname, String whereclause) throws DPFTRuntimeException {
		super(conn, tbname, whereclause);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected DPFTDbo getDboInstance(String dboname, HashMap<String, Object> d) {
		return new DPFTNotificationDbo(dboname, d, this);
	}
	
	@Override
	public DPFTDbo add() throws DPFTRuntimeException {
		DPFTDbo new_dbo = super.add();
		
		//init value
		new_dbo.setValue("process_sts", "I");
		return new_dbo;
	}
}
