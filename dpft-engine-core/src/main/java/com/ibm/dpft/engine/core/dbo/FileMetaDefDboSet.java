package com.ibm.dpft.engine.core.dbo;

import java.util.HashMap;

import com.ibm.dpft.engine.core.common.GlobalConstants;
import com.ibm.dpft.engine.core.connection.DPFTConnector;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;

public class FileMetaDefDboSet extends DPFTDboSet {

	public FileMetaDefDboSet(DPFTConnector conn, String tbname, String whereclause) throws DPFTRuntimeException {
		super(conn, tbname, whereclause);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected DPFTDbo getDboInstance(String dboname, HashMap<String, Object> d) {
		return new FileMetaDefDbo(dboname, d, this);
	}

	public String getControlFilePattern() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		return getMeta(GlobalConstants.FILE_META_ID_H_FILE);
		
	}
	
	public String getDataFilePattern() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		return getMeta(GlobalConstants.FILE_META_ID_D_FILE);
	}

	private String getMeta(String meta_id) throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		for(int i = 0; i < count(); i++){
			if(this.getDbo(i).getString("meta_id").equals(meta_id)){
				return this.getDbo(i).getString("value");
			}
		}
		return null;
	}

	public String getLocalDir() throws DPFTRuntimeException {
		return getMeta(GlobalConstants.FILE_META_ID_LOCAL_DIR);
	}

	public String getRemoteDir() throws DPFTRuntimeException {
		return getMeta(GlobalConstants.FILE_META_ID_FTP_DIR);
	}

	public String getFileEncoding() throws DPFTRuntimeException {
		return getMeta(GlobalConstants.FILE_META_ID_ENCODE);
	}

	public boolean isStaticMode() throws DPFTRuntimeException {
		String flg = getMeta(GlobalConstants.FILE_META_ID_STATIC_MODE);
		if(flg != null)
			return flg.equalsIgnoreCase("y");
		return false;
	}

	public boolean isContainHeader() throws DPFTRuntimeException {
		String flg = getMeta(GlobalConstants.FILE_META_ID_HEADER);
		if(flg != null)
			return flg.equalsIgnoreCase("y");
		return false;
	}

	public boolean needTransferValidate() throws DPFTRuntimeException {
		String flg = getMeta(GlobalConstants.FILE_META_ID_TRF_VALID);
		if(flg != null)
			return flg.equalsIgnoreCase("y");
		return false;
	}

	public boolean needCompress() throws DPFTRuntimeException {
		String flg = getMeta(GlobalConstants.FILE_META_ID_COMPRESS);
		if(flg != null)
			return flg.equalsIgnoreCase("y");
		return false;
	}

	public String getCompressFilePattern() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		return getMeta(GlobalConstants.FILE_META_ID_COMPRESS_FILE);
	}

	public boolean hasZFile() throws DPFTRuntimeException {
		String pattern = getMeta(GlobalConstants.FILE_META_ID_Z_FILE);
		if(pattern != null)
			return true;
		return false;
	}

	public String getZFilePattern() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		return getMeta(GlobalConstants.FILE_META_ID_Z_FILE);
	}
	
	public String getActionID() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		return getMeta(GlobalConstants.FILE_META_ID_H_FILE_ACT_ID);
	}

	public String getCDProfile() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		return getMeta(GlobalConstants.FILE_META_ID_CD_PROFILE);
	}

	public String getStaticLengUnit() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		return getMeta(GlobalConstants.FILE_META_ID_STATIC_LEN_UNIT);
	}

	public String getDelimeter() throws DPFTRuntimeException {
		// TODO Auto-generated method stub
		return getMeta(GlobalConstants.FILE_META_ID_DELIMETER);
	}
}
