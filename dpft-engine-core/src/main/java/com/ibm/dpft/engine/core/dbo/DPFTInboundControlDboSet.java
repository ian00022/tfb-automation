package com.ibm.dpft.engine.core.dbo;

import java.util.HashMap;

import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;

public class DPFTInboundControlDboSet extends DPFTDboSet {

	public DPFTInboundControlDboSet(DPFTConnector conn, String tbname, String whereclause) throws DPFTRuntimeException {
		super(conn, tbname, whereclause);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected DPFTDbo getDboInstance(String dboname, HashMap<String, Object> d) {
		return new DPFTInboundControlDbo(dboname, d, this);
	}

	public void taskComplete() throws DPFTRuntimeException {
		for(int i = 0; i < count(); i++){
			((DPFTInboundControlDbo)this.getDbo(i)).complete();
		}
		save();
	}

	public void error() throws DPFTRuntimeException {
		for(int i = 0; i < count(); i++){
			((DPFTInboundControlDbo)this.getDbo(i)).error();
		}
		save();
	}

	public void run() throws DPFTRuntimeException {
		for(int i = 0; i < count(); i++){
			((DPFTInboundControlDbo)this.getDbo(i)).run();
		}
		save();
	}

}
