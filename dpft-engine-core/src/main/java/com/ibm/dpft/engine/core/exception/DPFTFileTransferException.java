package com.ibm.dpft.engine.core.exception;

public class DPFTFileTransferException extends DPFTRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4445749735476463695L;

	public DPFTFileTransferException(String gid, String msgid, Object[] params) throws DPFTRuntimeException {
		super(gid, msgid, params);
	}
	
	public DPFTFileTransferException(String gid, String msgid) throws DPFTRuntimeException {
		super(gid, msgid);
	}
	
	public DPFTFileTransferException(String gid, String msgid, Object[] params, Throwable e) throws DPFTRuntimeException {
		super(gid, msgid, params, e);
	}
	
	public DPFTFileTransferException(String gid, String msgid, Throwable e) throws DPFTRuntimeException {
		super(gid, msgid, e);
	}
}
